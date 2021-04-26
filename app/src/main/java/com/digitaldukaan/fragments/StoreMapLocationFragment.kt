package com.digitaldukaan.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ProfileStatusAdapter2
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.request.StoreAddressRequest
import com.digitaldukaan.models.response.ProfileInfoResponse
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.models.response.StoreAddressResponse
import com.digitaldukaan.services.StoreAddressService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IStoreAddressServiceInterface
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.layout_store_map_location_fragment.*
import java.util.*

class StoreMapLocationFragment : BaseFragment(), LocationListener, IStoreAddressServiceInterface {

    private lateinit var mProfilePreviewResponse: ProfilePreviewSettingsKeyResponse
    private var mPosition: Int = 0
    private var mGoogleMap: GoogleMap? = null
    private var mIsSingleStep: Boolean = false
    private var mGoogleApiClient: FusedLocationProviderClient? = null
    private var mCurrentLatitude = 0.0
    private var mCurrentLongitude = 0.0
    private lateinit var locationManager: LocationManager
    private lateinit var supportMapFragment: SupportMapFragment
    private var lastLocation: Location? = null
    private val mMapStaticData = mStaticData.mStaticData.mMapStaticData
    private lateinit var mapBottomSheetLayout: View
    private lateinit var setLocationTextView: TextView
    private lateinit var stateTextView: TextView
    private var mCurrentMarker: Marker? = null
    private var mProfileInfoResponse: ProfileInfoResponse? = null

    companion object {

        private val TAG = StoreMapLocationFragment::class.simpleName

        fun newInstance(
            profilePreviewResponse: ProfilePreviewSettingsKeyResponse,
            position: Int,
            isSingleStep: Boolean,
            profileInfoResponse: ProfileInfoResponse?
        ): StoreMapLocationFragment {
            val fragment = StoreMapLocationFragment()
            fragment.mProfilePreviewResponse = profilePreviewResponse
            fragment.mPosition = position
            fragment.mIsSingleStep = isSingleStep
            fragment.mProfileInfoResponse = profileInfoResponse
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_store_map_location_fragment, container, false)
        locationManager = mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            val stepStr = if (mIsSingleStep) "" else "Step $mPosition : "
            setHeaderTitle("$stepStr${mProfilePreviewResponse.mHeadingText}")
            onBackPressed(this@StoreMapLocationFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        supportMapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mGoogleApiClient = LocationServices.getFusedLocationProviderClient(mActivity)
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
            return
        }
        getLastLocation()
        currentLocationImageView.setOnClickListener { getCurrentLocationOfDevice() }
        val completeAddressEditText: EditText = view.findViewById(R.id.completeAddressEditText)
        val pinCodeEditText: EditText = view.findViewById(R.id.pinCodeEditText)
        val cityEditText: EditText = view.findViewById(R.id.cityEditText)
        stateTextView = view.findViewById(R.id.stateTextView)
        val saveTextView: TextView = view.findViewById(R.id.saveTextView)
        val cityLayout: TextInputLayout = view.findViewById(R.id.cityLayout)
        val completeAddressLayout: TextInputLayout = view.findViewById(R.id.completeAddressLayout)
        val pinCodeLayout: TextInputLayout = view.findViewById(R.id.pinCodeLayout)
        setLocationTextView = view.findViewById(R.id.setLocationTextView)
        mapBottomSheetLayout = view.findViewById(R.id.mapBottomSheetLayout)
        completeAddressLayout.hint = mMapStaticData.completeAddressHint
        pinCodeLayout.hint = mMapStaticData.pinCodeTextHint
        cityLayout.hint = mMapStaticData.cityTextHint
        setLocationTextView.text = mMapStaticData.setLocationText
        stateTextView.text = getString(R.string.select_state)
        saveTextView.text = mMapStaticData.saveChangesText
        setLocationTextView.setOnClickListener {
            mapBottomSheetLayout.visibility = View.VISIBLE
            setLocationTextView.visibility = View.GONE
        }
        stateTextView.setOnClickListener {
            showStateSelectionDialog()
        }
        saveTextView.setOnClickListener {
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
            } else {
                val service = StoreAddressService()
                service.setServiceInterface(this)
                val address = completeAddressEditText.text.trim().toString()
                if (address.isEmpty()) {
                    completeAddressEditText.error =  getString(R.string.mandatory_field_message)
                    completeAddressEditText.requestFocus()
                    return@setOnClickListener
                }
                val request = StoreAddressRequest(
                    getStringDataFromSharedPref(Constants.STORE_ID).toInt(),
                    address,
                    mGoogleDrivenAddress,
                    mCurrentLatitude,
                    mCurrentLongitude,
                    pinCodeEditText.text.toString(),
                    cityEditText.text.toString(),
                    stateTextView.text.toString()
                )
                showProgressDialog(mActivity)
                service.updateStoreAddress(
                    request
                )
            }
        }
        mProfileInfoResponse?.mStoreItemResponse?.storeAddress?.run {
            pinCodeEditText.setText(pinCode)
            cityEditText.setText(city)
            completeAddressEditText.setText(address1)
            stateTextView.text = state
        }
        if (mIsSingleStep)  statusRecyclerView.visibility = View.GONE else {
            statusRecyclerView.apply {
                visibility = View.VISIBLE
                layoutManager = GridLayoutManager(mActivity, mProfileInfoResponse?.mTotalSteps?.toInt() ?: 0)
                adapter = ProfileStatusAdapter2(mProfileInfoResponse?.mTotalSteps?.toInt(), mPosition)
            }
        }
    }

    override fun onAlertDialogItemClicked(selectedStr: String?, id: Int, position: Int) {
        stateTextView.text = selectedStr
    }

    private fun getCurrentLocationOfDevice() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
            return
        }
        mGoogleApiClient?.lastLocation?.addOnCompleteListener { task ->
                val location = task.result
                if (location != null) {
                    mCurrentLatitude = location.latitude
                    mCurrentLongitude = location.longitude
                    showCurrentLocationMarkers(location.latitude, location.longitude)
                } else {
                    showToast("Location is detecting as null")
                }
            }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
            return
        }
        //val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, this)
        mGoogleApiClient?.lastLocation?.addOnCompleteListener(mActivity) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                mCurrentLatitude = lastLocation?.latitude ?: 0.0
                mCurrentLongitude = lastLocation?.longitude ?: 0.0
                supportMapFragment.getMapAsync{
                    mGoogleMap = it
                    showCurrentLocationMarkers(mCurrentLatitude, mCurrentLongitude)
                    mGoogleMap?.setOnCameraMoveListener { Log.d(TAG, "dragging start setOnCameraMoveListener()") }
                    mGoogleMap?.setOnCameraMoveStartedListener {
                        Log.d(TAG,"dragging start setOnCameraMoveStartedListener()")
                        if (mapBottomSheetLayout.visibility  == View.VISIBLE) {
                            mapBottomSheetLayout.visibility = View.GONE
                            setLocationTextView.visibility = View.VISIBLE
                        }
                    }
                    mGoogleMap?.setOnMarkerDragListener(object :GoogleMap.OnMarkerDragListener {

                        override fun onMarkerDragEnd(marker: Marker?) {
                            if (null != mCurrentMarker) mCurrentMarker?.remove()
                            showCurrentLocationMarkers(marker?.position?.latitude ?: 0.0, marker?.position?.longitude ?: 0.0)
                        }

                        override fun onMarkerDragStart(marker: Marker?) {
                        }

                        override fun onMarkerDrag(marker: Marker?) {
                        }

                    })
                }
            } else {
                showToast("No location detected. Make sure location is enabled on the device.")
                mCurrentLatitude = mProfileInfoResponse?.mStoreItemResponse?.storeAddress?.latitude ?: 0.0
                mCurrentLongitude = mProfileInfoResponse?.mStoreItemResponse?.storeAddress?.longitude ?: 0.0
                showCurrentLocationMarkers(mCurrentLatitude, mCurrentLongitude)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted.
                    getLastLocation()
                }
                else -> {
                    showShortSnackBar("Permission was denied")
                }
            }
        }
    }

    private var mGoogleDrivenAddress :String ? = ""

    private fun showCurrentLocationMarkers(lat: Double, lng: Double) {
        mGoogleDrivenAddress = getAddress()
        val markerOptions = MarkerOptions().title("current location").position(LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker)).draggable(true).snippet(mGoogleDrivenAddress)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 11f)
        mGoogleMap?.animateCamera(cameraUpdate)
        mCurrentMarker = mGoogleMap?.addMarker(markerOptions)
        mCurrentMarker?.showInfoWindow()
    }

    private fun getAddress(): String? {
        val geoCoder = Geocoder(mActivity, Locale.getDefault())
        val addressList = geoCoder.getFromLocation(mCurrentLatitude, mCurrentLongitude, 1)
        return addressList[0].getAddressLine(0).toString()
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            mActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (shouldProvideRationale) {
            showShortSnackBar(
                "Location permission is needed for core functionality"
            )
        } else {
            startLocationPermissionRequest()
        }
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Constants.LOCATION_REQUEST_CODE)
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged() Latitude: " + location.latitude + " , Longitude: " + location.longitude)
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        Log.d(TAG, "onStatusChanged :: p0 :: $p0, p1 :: $p1, p2:: $p2")
    }

    override fun onProviderEnabled(p0: String?) {
        Log.d(TAG, "onProviderEnabled :: $p0")
    }

    override fun onProviderDisabled(p0: String?) {
        Log.d(TAG, "onProviderDisabled :: $p0")
    }

    override fun onStoreAddressResponse(response: StoreAddressResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                if (!mIsSingleStep) {
                    StaticInstances.sStepsCompletedList?.run {
                        for (completedItem in this) {
                            if (completedItem.action == Constants.ACTION_LOCATION) {
                                completedItem.isCompleted = true
                                break
                            }
                        }
                        switchToInCompleteProfileFragment(mProfileInfoResponse)
                    }
                } else {
                    mActivity.onBackPressed()
                }
            } else showToast(response.mMessage)
        }
    }

    override fun onStoreAddressServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}