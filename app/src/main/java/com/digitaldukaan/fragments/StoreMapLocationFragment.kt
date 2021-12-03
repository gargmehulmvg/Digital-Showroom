package com.digitaldukaan.fragments

import android.Manifest
import android.app.AlertDialog
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
import androidx.core.content.ContextCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.StoreAddressRequest
import com.digitaldukaan.models.response.*
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
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_store_map_location_fragment.*
import java.util.*

class StoreMapLocationFragment : BaseFragment(), LocationListener, IStoreAddressServiceInterface {

    private var mPosition: Int = 0
    private var mGoogleMap: GoogleMap? = null
    private var mIsSingleStep: Boolean = false
    private var mGoogleApiClient: FusedLocationProviderClient? = null
    private var mCurrentLatitude = 0.0
    private var mCurrentLongitude = 0.0
    private lateinit var locationManager: LocationManager
    private lateinit var supportMapFragment: SupportMapFragment
    private var lastLocation: Location? = null
    private var mMapStaticData: MapLocationStaticResponseData? = null
    private var mapBottomSheetLayout: View? = null
    private var setLocationTextView: TextView? = null
    private var stateTextView: TextView? = null
    private var mCurrentMarker: Marker? = null
    private var mGoogleDrivenAddress :String ? = ""
    private var mService = StoreAddressService()
    private var mStoreLocationResponse: GetStoreLocationResponse? = null

    companion object {

        fun newInstance(position: Int, isSingleStep: Boolean): StoreMapLocationFragment {
            val fragment = StoreMapLocationFragment()
            fragment.mPosition = position
            fragment.mIsSingleStep = isSingleStep
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "StoreMapLocationFragment"
        mContentView = inflater.inflate(R.layout.layout_store_map_location_fragment, container, false)
        locationManager = mActivity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mService.setServiceInterface(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgressDialog(mActivity)
        mService.getStoreLocation()
    }

    private fun setupLocationUI() {
        currentLocationImageView.setOnClickListener { getCurrentLocationOfDevice() }
        val completeAddressEditText: EditText? = mContentView?.findViewById(R.id.completeAddressEditText)
        val pinCodeEditText: EditText? = mContentView?.findViewById(R.id.pinCodeEditText)
        val cityEditText: EditText? = mContentView?.findViewById(R.id.cityEditText)
        stateTextView = mContentView?.findViewById(R.id.stateTextView)
        val saveTextView: TextView? = mContentView?.findViewById(R.id.saveTextView)
        val cityLayout: TextInputLayout? = mContentView?.findViewById(R.id.cityLayout)
        val completeAddressLayout: TextInputLayout? = mContentView?.findViewById(R.id.completeAddressLayout)
        val pinCodeLayout: TextInputLayout? = mContentView?.findViewById(R.id.pinCodeLayout)
        setLocationTextView = mContentView?.findViewById(R.id.setLocationTextView)
        mapBottomSheetLayout = mContentView?.findViewById(R.id.mapBottomSheetLayout)
        completeAddressLayout?.hint = mMapStaticData?.completeAddressHint
        pinCodeLayout?.hint = mMapStaticData?.pinCodeTextHint
        cityLayout?.hint = mMapStaticData?.cityTextHint
        setLocationTextView?.text = mMapStaticData?.setLocationText
        stateTextView?.text = getString(R.string.select_state)
        saveTextView?.text = mMapStaticData?.saveChangesText
        setLocationTextView?.setOnClickListener {
            mapBottomSheetLayout?.visibility = View.VISIBLE
            setLocationTextView?.visibility = View.GONE
        }
        stateTextView?.setOnClickListener {
            showStateSelectionDialog()
        }
        saveTextView?.setOnClickListener {
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
            } else {
                val address = completeAddressEditText?.text?.trim().toString()
                if (isEmpty(address)) {
                    completeAddressEditText?.error =  getString(R.string.mandatory_field_message)
                    completeAddressEditText?.requestFocus()
                    return@setOnClickListener
                }
                val request = StoreAddressRequest(
                    getStringDataFromSharedPref(Constants.STORE_ID).toInt(),
                    address,
                    mGoogleDrivenAddress,
                    mCurrentLatitude,
                    mCurrentLongitude,
                    pinCodeEditText?.text.toString(),
                    cityEditText?.text.toString(),
                    stateTextView?.text.toString()
                )
                showProgressDialog(mActivity)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SAVE_ADDRESS_CHANGES,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                mService.updateStoreAddress(request)
            }
        }
        mStoreLocationResponse?.storeAddress?.let { address ->
            pinCodeEditText?.setText(address.pinCode)
            cityEditText?.setText(address.city)
            completeAddressEditText?.setText(address.address1)
            stateTextView?.text = if (isEmpty(address.state)) getString(R.string.select_state) else address.state
        }
    }

    private fun checkLocationPermission(): Boolean {
        mActivity?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Constants.LOCATION_REQUEST_CODE)
                return true
            }
        }
        return false
    }

    override fun onAlertDialogItemClicked(selectedStr: String?, id: Int, position: Int) {
        stateTextView?.text = selectedStr
    }

    private fun getCurrentLocationOfDevice() {
        if (checkLocationPermission()) return
        mGoogleApiClient?.lastLocation?.addOnCompleteListener { task ->
                val location = task.result
                if (null != location) {
                    mCurrentLatitude = location.latitude
                    mCurrentLongitude = location.longitude
                    showCurrentLocationMarkers(location.latitude, location.longitude)
                } else {
                    if (!isLocationEnabledInSettings(mActivity)) {
                        openLocationSettings(true)
                    }
                }
            }
    }

    private fun getLastLocation() {
        if (checkLocationPermission()) return
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, this)
        mGoogleApiClient?.lastLocation?.addOnCompleteListener(mActivity) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                supportMapFragment.getMapAsync{
                    mGoogleMap = it
                    showCurrentLocationMarkers(mCurrentLatitude, mCurrentLongitude)
                    mGoogleMap?.setOnCameraMoveListener { Log.d(TAG, "dragging start setOnCameraMoveListener()") }
                    mGoogleMap?.setOnCameraMoveStartedListener {
                        Log.d(TAG,"dragging start setOnCameraMoveStartedListener()")
                        if (mapBottomSheetLayout?.visibility  == View.VISIBLE) {
                            mapBottomSheetLayout?.visibility = View.GONE
                            setLocationTextView?.visibility = View.VISIBLE
                        }
                    }
                    mGoogleMap?.setOnMarkerDragListener(object :GoogleMap.OnMarkerDragListener {

                        override fun onMarkerDragEnd(marker: Marker?) {
                            if (mCurrentMarker != null) mCurrentMarker?.remove()
                            mCurrentLatitude = marker?.position?.latitude ?: 0.0
                            mCurrentLongitude = marker?.position?.longitude ?: 0.0
                            showCurrentLocationMarkers(mCurrentLatitude, mCurrentLongitude)
                        }

                        override fun onMarkerDragStart(marker: Marker?) {
                            Log.d(TAG, "onMarkerDragStart: ")
                        }

                        override fun onMarkerDrag(marker: Marker?) {
                            Log.d(TAG, "onMarkerDrag: ")
                        }

                    })
                }
            } else {
                if (!isLocationEnabledInSettings(mActivity)) openLocationSettings(true)
                mCurrentLatitude = mStoreLocationResponse?.storeAddress?.latitude ?: 0.0
                mCurrentLongitude = mStoreLocationResponse?.storeAddress?.longitude ?: 0.0
                showCurrentLocationMarkers(mCurrentLatitude, mCurrentLongitude)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (Constants.LOCATION_REQUEST_CODE == requestCode) {
                when {
                    grantResults.isEmpty() -> Log.d(TAG, "User interaction was cancelled.")
                    PackageManager.PERMISSION_GRANTED == grantResults[0] -> {
                        getLastLocation()
                        setupLocationUI()
                    }
                    else -> {
                        showShortSnackBar("Permission was denied, Please allow location permission from settings to continue", true, R.drawable.ic_close_red)
                        mActivity?.onBackPressed()
                    }
                }
            }
        }
    }

    private fun showCurrentLocationMarkers(lat: Double, lng: Double) {
        mGoogleDrivenAddress = getAddress()
        val markerOptions = MarkerOptions().title("current location").position(LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker)).draggable(true).snippet(mGoogleDrivenAddress)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 11f)
        mGoogleMap?.animateCamera(cameraUpdate)
        mCurrentMarker?.remove()
        mCurrentMarker = mGoogleMap?.addMarker(markerOptions)
        mCurrentMarker?.showInfoWindow()
    }

    private fun getAddress(): String? {
        return try {
            val geoCoder = Geocoder(mActivity, Locale.getDefault())
            val addressList = geoCoder.getFromLocation(mCurrentLatitude, mCurrentLongitude, 1)
            if (isNotEmpty(addressList)) addressList[0].getAddressLine(0).toString() else ""
        } catch (e: Exception) {
            Log.e(TAG, "getAddress: ${e.message}", e)
            ""
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged() Latitude: " + location.latitude + " , Longitude: " + location.longitude)
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        Log.d(TAG, "onStatusChanged :: p0 :: $p0, p1 :: $p1, p2:: $p2")
    }

    override fun onStoreAddressResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                mActivity?.onBackPressed()
            } else showToast(response.mMessage)
        }
    }

    override fun onGetStoreLocationResponse(commonApiResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonApiResponse.mIsSuccessStatus) {
                mStoreLocationResponse = Gson().fromJson(commonApiResponse.mCommonDataStr, GetStoreLocationResponse::class.java)
                mCurrentLatitude = mStoreLocationResponse?.storeAddress?.latitude ?: 0.0
                mCurrentLongitude = mStoreLocationResponse?.storeAddress?.longitude ?: 0.0
                ToolBarManager.getInstance().apply {
                    hideToolBar(mActivity, false)
                    val stepStr = if (mIsSingleStep) "" else "Step $mPosition : "
                    headerTitle = "$stepStr${mStoreLocationResponse?.mMapStaticData?.headingPage}"
                    onBackPressed(this@StoreMapLocationFragment)
                    hideBackPressFromToolBar(mActivity, false)
                }
                mMapStaticData = mStoreLocationResponse?.mMapStaticData
                supportMapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
                mGoogleApiClient = LocationServices.getFusedLocationProviderClient(mActivity)
                if (checkLocationPermissionWithDialog()) return@runTaskOnCoroutineMain
                getLastLocation()
                setupLocationUI()
            }
        }
    }

    override fun onStoreAddressServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    private fun checkLocationPermissionWithDialog(): Boolean {
        mActivity?.let {
            return if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(it).apply {
                        setTitle("Location Permission")
                        setMessage("Please allow Location permission to continue")
                        setPositiveButton(R.string.ok) { _, _ -> ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), Constants.LOCATION_REQUEST_CODE) }
                    }.create().show()
                } else ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), Constants.LOCATION_REQUEST_CODE)
                true
            } else false
        }
        return false
    }

    override fun onProviderEnabled(provider: String) = Unit

    override fun onProviderDisabled(provider: String) = Unit

}