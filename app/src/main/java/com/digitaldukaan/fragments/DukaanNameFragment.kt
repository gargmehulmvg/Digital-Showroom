package com.digitaldukaan.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
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
import androidx.transition.TransitionInflater
import com.appsflyer.AppsFlyerLib
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.CreateStoreRequest
import com.digitaldukaan.models.response.BusinessNameStaticText
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.CreateStoreResponse
import com.digitaldukaan.services.DukaanNameService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ICreateStoreServiceInterface
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_dukaan_name_fragment.*

class DukaanNameFragment : BaseFragment(), ICreateStoreServiceInterface {

    private var mDukaanNameStaticData: BusinessNameStaticText? = null
    private lateinit var locationManager: LocationManager
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var mCurrentLatitude = 0.0
    private var mCurrentLongitude = 0.0
    private var lastLocation: Location? = null
    private var mUserId: String = ""

    companion object {
        fun newInstance(userId: String): DukaanNameFragment {
            val fragment = DukaanNameFragment()
            fragment.mUserId = userId
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        locationManager = mActivity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mActivity?.let { context -> fusedLocationClient = LocationServices.getFusedLocationProviderClient(context) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "DukaanNameFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_dukaan_name_fragment, container, false)
        mDukaanNameStaticData = StaticInstances.sStaticData?.mBusinessNameStaticText
        if (checkLocationPermissionWithDialog()) getLastLocation()
        if (!isLocationEnabledInSettings(mActivity)) openLocationSettings(false)
        return mContentView
    }

    private fun checkLocationPermissionWithDialog(): Boolean {
        mActivity?.let {
            return if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(mActivity).apply {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dukaanNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString()
                nextTextView?.isEnabled = str.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged: ")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: ")
            }
        })
        setupUIFromStaticData()
        if (sIsInvitationAvailable) showStaffInvitationDialog()
    }

    private fun setupUIFromStaticData() {
        enterDukaanNameHeading?.text = mDukaanNameStaticData?.heading_page
        subHeadingTextView?.text = mDukaanNameStaticData?.sub_heading_page
        dukaanNameEditText?.hint = mDukaanNameStaticData?.hint_enter_here
        nextTextView?.text = mDukaanNameStaticData?.cta_text
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            nextTextView?.id -> {
                val dukaanName = dukaanNameEditText?.text?.trim().toString()
                if (isEmpty(dukaanName)) {
                    dukaanNameEditText?.apply {
                        requestFocus()
                        showKeyboard()
                        error = getString(R.string.mandatory_field_message)
                    }
                } else {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                    } else {
                        val service = DukaanNameService()
                        service.setServiceInterface(this)
                        val storeIdStr = PrefsManager.getStringDataFromSharedPref(Constants.USER_ID)
                        val request = CreateStoreRequest(
                            phone = PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER),
                            userId = if (storeIdStr.isNotEmpty()) storeIdStr.toInt() else 0,
                            storeName = dukaanName,
                            secretKey = Constants.APP_SECRET_KEY,
                            languageId = 1,
                            referencePhone = StaticInstances.sAppFlyerRefMobileNumber,
                            appsflyerId = AppsFlyerLib.getInstance().getAttributionId(mActivity),
                            cleverTapId = StaticInstances.sCleverTapId,
                            latitude = mCurrentLatitude,
                            longitude = mCurrentLongitude
                        )
                        showProgressDialog(mActivity)
                        service.createStore(request)
                    }
                }
            }
        }
    }

    override fun onCreateStoreResponse(commonApiResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonApiResponse.mIsSuccessStatus) {
                val createStoreResponse = Gson().fromJson<CreateStoreResponse>(commonApiResponse.mCommonDataStr, CreateStoreResponse::class.java)
                PrefsManager.storeStringDataInSharedPref(Constants.STORE_ID, "${createStoreResponse.storeId}")
                PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, "${createStoreResponse.storeInfo?.name}")
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_ENTER_NAME,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID          to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PHONE             to PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER),
                        AFInAppEventParameterName.USER_ID           to PrefsManager.getStringDataFromSharedPref(Constants.USER_ID),
                        AFInAppEventParameterName.STORE_NAME        to createStoreResponse?.storeInfo?.name,
                        AFInAppEventParameterName.STORE_TYPE        to AFInAppEventParameterName.STORE_TYPE_DUKAAN,
                        AFInAppEventParameterName.VERIFY_PHONE      to "1",
                        AFInAppEventParameterName.REFERENCE_PHONE   to StaticInstances.sAppFlyerRefMobileNumber
                    )
                )
                launchFragment(CreateStoreFragment.newInstance(), true)
            } else showShortSnackBar(commonApiResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onCreateStoreServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onStart() {
        super.onStart()
        getLastLocation()
    }

    private fun getLastLocation() {
        mActivity?.let { context ->
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Constants.LOCATION_REQUEST_CODE)
                return
            }
        }
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocation?.let { location ->
            Log.d(TAG, "getLocation() Latitude: ${location.latitude} , Longitude: ${location.longitude}")
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, this)
        mActivity?.let {
            fusedLocationClient?.lastLocation?.addOnCompleteListener(it) { task ->
                if (task.isSuccessful && null != task.result) {
                    lastLocation = task.result
                    mCurrentLatitude = lastLocation?.latitude ?: 0.0
                    mCurrentLongitude = lastLocation?.longitude ?: 0.0
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (sIsInvitationAvailable) showStaffInvitationDialog()
            when {
                grantResults.isEmpty() -> Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    getLastLocation()
                }
                else -> {
                    showToast("Permission was denied")
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        logoutFromApplication(true)
        return true
    }

}