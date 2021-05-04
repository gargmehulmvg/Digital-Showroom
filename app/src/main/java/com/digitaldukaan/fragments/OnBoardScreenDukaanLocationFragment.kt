package com.digitaldukaan.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.StoreAddressRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.OnBoardStep2StaticResponseData
import com.digitaldukaan.services.StoreAddressService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IStoreAddressServiceInterface
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.layout_on_board_screen_dukaan_location_fragment.*


class OnBoardScreenDukaanLocationFragment : BaseFragment(), IStoreAddressServiceInterface, LocationListener {

    private var mDukaanLocationStaticData: OnBoardStep2StaticResponseData? = null
    private var mCurrentLatitude = 0.0
    private var mCurrentLongitude = 0.0
    private lateinit var locationManager: LocationManager

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null

    companion object {
        fun newInstance(): OnBoardScreenDukaanLocationFragment {
            return OnBoardScreenDukaanLocationFragment()
        }
        private val TAG = OnBoardScreenDukaanLocationFragment::class.simpleName
    }

    override fun onStart() {
        super.onStart()
        getLastLocation()
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
            return
        }
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocation?.run {
            Log.d(TAG, "getLocation() Latitude: $latitude , Longitude: $longitude")
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, this)
        fusedLocationClient?.lastLocation?.addOnCompleteListener(mActivity) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                mCurrentLatitude = lastLocation?.latitude ?: 0.0
                mCurrentLongitude = lastLocation?.longitude ?: 0.0
            }
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Constants.LOCATION_REQUEST_CODE)
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
                    showToast("Permission was denied")
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
        mDukaanLocationStaticData = mStaticData.mStaticData.mOnBoardStep2StaticData
        if (checkLocationPermissionWithDialog()) {
            getLastLocation()
        }
        if (!isLocationEnabledInSettings(mActivity)) {
            openLocationSettings(false)
        }
        return mContentView
    }

    private fun checkLocationPermissionWithDialog(): Boolean {
        return if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(mActivity).apply {
                    setTitle("Location Permission")
                    setMessage("Please allow Location permission to continue")
                    setPositiveButton(R.string.ok) { _, _ -> ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), Constants.LOCATION_REQUEST_CODE)
                    }
                }.create().show()
            } else ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), Constants.LOCATION_REQUEST_CODE)
            true
        } else false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
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
        Log.d(TAG, "onLocationChanged() Latitude: " + location.latitude + " , Longitude: " + location.longitude)
        mCurrentLatitude = location.latitude
        mCurrentLongitude = location.longitude
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

    private fun setupUIFromStaticData() {
        step2TextView?.text = mDukaanLocationStaticData?.mStepCount
        dukaanLocationHeading?.text = mDukaanLocationStaticData?.mHeading
        dukaanLocationEditText?.hint = mDukaanLocationStaticData?.mTitleHinText
        nextTextView?.text = mDukaanLocationStaticData?.mNextButton
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backImageView.id -> {
                mActivity.onBackPressed()
            }
            nextTextView.id -> {
                val storeLocation = dukaanLocationEditText?.text?.trim().toString()
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
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_VERIFY_LOCATION_SET,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(
                                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                AFInAppEventParameterName.ADDRESS to storeLocation,
                                AFInAppEventParameterName.GOOGLE_ADDRESS to storeLocation,
                                AFInAppEventParameterName.LATITUDE to "$mCurrentLatitude",
                                AFInAppEventParameterName.LONGITUDE to "$mCurrentLongitude",
                                AFInAppEventParameterName.IS_MERCHANT to "1"
                            )
                        )
                        val request = StoreAddressRequest(
                            PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID).toInt(),
                            latitude = mCurrentLatitude,
                            longitude = mCurrentLongitude,
                            address = storeLocation
                        )
                        showProgressDialog(mActivity)
                        service.updateStoreAddress(request)
                    }
                }
            }
        }
    }

    override fun onStoreAddressResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                launchFragment(OnBoardScreenBankDetailsFragment.newInstance(), true)
            } else showToast(response.mMessage)
        }
    }

    override fun onStoreAddressServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }
}