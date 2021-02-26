package com.digitaldukaan.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.StoreAddressRequest
import com.digitaldukaan.services.StoreAddressService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IStoreAddressServiceInterface
import kotlinx.android.synthetic.main.on_board_screen_dukaan_location_fragment.*
import okhttp3.ResponseBody


class OnBoardScreenDukaanLocationFragment : BaseFragment(), IStoreAddressServiceInterface, LocationListener {

    private val mDukaanLocationStaticData = mStaticData.mStaticData.mOnBoardStep2StaticData
    private var mCurrentLatitude = 0.0
    private var mCurrentLongitude = 0.0
    private lateinit var locationManager: LocationManager

    companion object {
        private val TAG = OnBoardScreenDukaanLocationFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.on_board_screen_dukaan_location_fragment, container, false)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Permission Granted")
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
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                lastKnownLocation?.run {
                    showToast("getLocation() Latitude: $latitude , Longitude: $longitude")
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, this)
            } else {
                showToast("Permission Denied")
            }
        }
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
                            storeLocation
                        )
                        showProgressDialog(mActivity)
                        service.updateStoreAddress(
                            getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN),
                            request
                        )
                    }
                }
            }
        }
    }

    override fun onStoreAddressResponse(response: ResponseBody) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            /*if (response.mStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                launchFragment(OnBoardScreenDukaanLocationFragment(), true)
            } else showToast(response.mMessage)*/
            showToast(response.string())
        }
    }

    override fun onStoreAddressServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }
}