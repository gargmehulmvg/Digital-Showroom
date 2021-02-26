package com.digitaldukaan.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_store_map_location.*

class StoreMapLocationFragment : BaseFragment(), LocationListener {

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

    companion object {

        private val TAG = StoreMapLocationFragment::class.simpleName

        fun newInstance(
            profilePreviewResponse: ProfilePreviewSettingsKeyResponse,
            position: Int,
            isSingleStep: Boolean
        ): StoreMapLocationFragment {
            val fragment = StoreMapLocationFragment()
            fragment.mProfilePreviewResponse = profilePreviewResponse
            fragment.mPosition = position
            fragment.mIsSingleStep = isSingleStep
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.fragment_store_map_location, container, false)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions()
                return
            }
        }
        getLastLocation()
        currentLocationImageView.setOnClickListener { getCurrentLocationOfDevice() }
        val completeAddressEditText: EditText = view.findViewById(R.id.completeAddressEditText)
        val pinCodeEditText: EditText = view.findViewById(R.id.pinCodeEditText)
        val cityEditText: EditText = view.findViewById(R.id.cityEditText)
        val stateTextView: TextView = view.findViewById(R.id.stateTextView)
        val saveTextView: TextView = view.findViewById(R.id.saveTextView)
        val cityLayout: TextInputLayout = view.findViewById(R.id.cityLayout)
        val completeAddressLayout: TextInputLayout = view.findViewById(R.id.completeAddressLayout)
        val pinCodeLayout: TextInputLayout = view.findViewById(R.id.pinCodeLayout)
        val setLocationTextView: TextView = view.findViewById(R.id.setLocationTextView)
        val mapBottomSheetLayout: View = view.findViewById(R.id.mapBottomSheetLayout)
        completeAddressLayout.hint = mMapStaticData.completeAddressHint
        pinCodeLayout.hint = mMapStaticData.pinCodeTextHint
        cityLayout.hint = mMapStaticData.cityTextHint
        setLocationTextView.text = mMapStaticData.setLocationText
        stateTextView.text = "Select State"
        saveTextView.text = mMapStaticData.saveChangesText
        setLocationTextView.setOnClickListener {
            mapBottomSheetLayout.visibility = View.VISIBLE
            setLocationTextView.visibility = View.GONE
        }
    }

    private fun getCurrentLocationOfDevice() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions()
                return
            }
        }
        mGoogleApiClient?.lastLocation
            ?.addOnCompleteListener { task ->
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions()
                return
            }
        }
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocation?.run {
            showToast("getLocation() Latitude: $latitude , Longitude: $longitude")
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, this)
        mGoogleApiClient?.lastLocation?.addOnCompleteListener(mActivity) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                mCurrentLatitude = lastLocation?.latitude ?: 0.0
                mCurrentLongitude = lastLocation?.longitude ?: 0.0
                supportMapFragment.getMapAsync{
                    mGoogleMap = it
                    showCurrentLocationMarkers(mCurrentLatitude, mCurrentLongitude)
                }
            } else {
                showToast("No location detected. Make sure location is enabled on the device.")
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

    private fun showCurrentLocationMarkers(lat: Double, lng: Double) {
        val markerOptions = MarkerOptions().title("current location").position(LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker))
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 11f)
        mGoogleMap?.animateCamera(cameraUpdate)
        mGoogleMap?.addMarker(markerOptions)
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
        ActivityCompat.requestPermissions(
            mActivity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            Constants.LOCATION_REQUEST_CODE
        )
    }

    override fun onLocationChanged(location: Location) {
        showToast("onLocationChanged() Latitude: " + location.latitude + " , Longitude: " + location.longitude)
    }


    private fun showEditStoreNameBottomSheet() {
        /*val locationBottomSheet = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_map_location, mActivity.findViewById(R.id.bottomSheetContainer))
        locationBottomSheet.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            val completeAddressEditText: EditText = view.findViewById(R.id.completeAddressEditText)
            val pinCodeEditText: EditText = view.findViewById(R.id.pinCodeEditText)
            val cityEditText: EditText = view.findViewById(R.id.cityEditText)
            val stateTextView: TextView = view.findViewById(R.id.stateTextView)
            val saveTextView: View = view.findViewById(R.id.saveTextView)
            completeAddressEditText.hint = mMapStaticData.completeAddressHint
            pinCodeEditText.hint = mMapStaticData.pinCodeTextHint
            cityEditText.hint = mMapStaticData.cityTextHint
            stateTextView.text = "Select State"
            saveTextView.setOnClickListener {

            }
            show()
        }*/
    }

}