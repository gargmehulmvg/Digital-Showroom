package com.digitaldukaan.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.StoreAddressRequest
import com.digitaldukaan.models.response.StoreAddressResponse
import com.digitaldukaan.services.StoreAddressService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IStoreAddressServiceInterface
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.layout_on_board_screen_dukaan_location_fragment.*


class OnBoardScreenDukaanLocationFragment : BaseFragment(), IStoreAddressServiceInterface, LocationListener {

    private val mDukaanLocationStaticData = mStaticData.mStaticData.mOnBoardStep2StaticData
    private var mCurrentLatitude = 0.0
    private var mCurrentLongitude = 0.0
    private lateinit var locationManager: LocationManager

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null

    companion object {
        private val TAG = OnBoardScreenDukaanLocationFragment::class.simpleName
    }

    override fun onStart() {
        super.onStart()
        getLastLocation()
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
        fusedLocationClient?.lastLocation?.addOnCompleteListener(mActivity) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                mCurrentLatitude = lastLocation?.latitude ?: 0.0
                mCurrentLongitude = lastLocation?.longitude ?: 0.0
            } else {
                showToast("No location detected. Make sure location is enabled on the device.")
            }
        }
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            mActivity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            Constants.LOCATION_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            mActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (shouldProvideRationale) {
            showCustomSnackBar(
                "Location permission is needed for core functionality"
            )
        } else {
            startLocationPermissionRequest()
        }
    }

    private fun showCustomSnackBar(mainTextStringId: String) = showToast(mainTextStringId)

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
                    showCustomSnackBar("Permission was denied")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_on_board_screen_dukaan_location_fragment, container, false)
        getLocation()
        return mContentView
    }

    private fun getLocation() {
        context?.let {
            if (!(ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(
                    mActivity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    Constants.LOCATION_REQUEST_CODE
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dukaanLocationEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString()
                nextTextView.isEnabled = str.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        setupUIFromStaticData()

    }

    override fun onLocationChanged(location: Location) {
        showToast("onLocationChanged() Latitude: " + location.latitude + " , Longitude: " + location.longitude)
    }

    private fun setupUIFromStaticData() {
        step2TextView.text = mDukaanLocationStaticData.mStepCount
        dukaanLocationHeading.text = mDukaanLocationStaticData.mHeading
        dukaanLocationEditText.hint = mDukaanLocationStaticData.mTitleHinText
        nextTextView.text = mDukaanLocationStaticData.mNextButton
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backImageView.id -> {
                mActivity.onBackPressed()
            }
            nextTextView.id -> {
                val storeLocation = dukaanLocationEditText.text.trim().toString()
                if (storeLocation.isEmpty()) {
                    dukaanLocationEditText.requestFocus()
                    dukaanLocationEditText.showKeyboard()
                    dukaanLocationEditText.error = getString(R.string.mandatory_field_message)
                } else {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                    } else {
                        val service = StoreAddressService()
                        service.setServiceInterface(this)
                        val request = StoreAddressRequest(
                            getStringDataFromSharedPref(Constants.STORE_ID).toInt(),
                            latitude = mCurrentLatitude,
                            longitude = mCurrentLongitude,
                            address = storeLocation
                        )
                        showProgressDialog(mActivity)
                        service.updateStoreAddress(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
                    }
                }
            }
        }
    }

    override fun onStoreAddressResponse(response: StoreAddressResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                launchFragment(HomeFragment.newInstance(), true)
            } else showToast(response.mMessage)
        }
    }

    override fun onStoreAddressServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }
}