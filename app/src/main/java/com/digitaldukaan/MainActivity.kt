package com.digitaldukaan

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.InAppNotificationButtonListener
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener
import com.digitaldukaan.constants.*
import com.digitaldukaan.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_LABELED
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.messaging.FirebaseMessaging
import com.truecaller.android.sdk.TruecallerSDK
import kotlinx.android.synthetic.main.activity_main2.*
import java.util.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, CTPushNotificationListener, InAppNotificationButtonListener {

    private var manager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null

    companion object {
        private val mNetworkChangeListener = NetworkChangeListener()
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ToolBarManager.getInstance().setupToolbar(toolbarLayout)
        setupBottomNavigation()
        PrefsManager.setPrefsManager(this)
        requestInAppReviewObject()
        AppEventsManager.setAppEventsManager(this)
        if (isEmpty(StaticInstances.sAppSessionId)) StaticInstances.sAppSessionId = RandomStringGenerator(16).nextString()
        if (isEmpty(PrefsManager.getStringDataFromSharedPref(PrefsManager.APP_INSTANCE_ID))) PrefsManager.storeStringDataInSharedPref(PrefsManager.APP_INSTANCE_ID, RandomStringGenerator(16).nextString())
        Log.d(TAG, "appSessionID :: ${StaticInstances.sAppSessionId}")
        Log.d(TAG, "appInstanceID :: ${PrefsManager.getStringDataFromSharedPref(PrefsManager.APP_INSTANCE_ID)}")
        Log.d(TAG, "userMobileNumber :: ${PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER)}")
        val intentUri = intent?.data
        launchFragment(SplashFragment.newInstance(intentUri), true)
        handlingFirebaseToken()
        AppsFlyerLib.getInstance().setCustomerUserId(PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER))
        CleverTapAPI.getDefaultInstance(this)?.apply {
            setInAppNotificationButtonListener(this@MainActivity)
        }
        val builder: StrictMode.VmPolicy.Builder  = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    private fun handlingFirebaseToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                try {
                    if (it.isComplete) {
                        StaticInstances.sFireBaseMessagingToken = it.result.toString()
                        Log.d(TAG, "onCreate :: FIREBASE TOKEN :: ${it.result}")
                        AppsFlyerLib.getInstance().updateServerUninstallToken(this, StaticInstances.sFireBaseMessagingToken)
                        CleverTapAPI.getDefaultInstance(this)?.pushFcmRegistrationId(StaticInstances.sFireBaseMessagingToken, true)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = false,
                        data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), "Exception Point" to "handlingFirebaseToken", "Exception Message" to e.message, "Exception Logs" to e.toString())
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = false,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), "Exception Point" to "handlingFirebaseToken", "Exception Message" to e.message, "Exception Logs" to e.toString())
            )
        }
    }

    override fun onStart() {
        try {
            val intentFiler = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(mNetworkChangeListener, intentFiler)
        } catch (e: Exception) {
            Log.e(TAG, "onStart: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = false,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), "Exception Point" to "Main Activity : onStart", "Exception Message" to e.message, "Exception Logs" to e.toString())
            )
        }
        super.onStart()
    }

    override fun onStop() {
        try {
            unregisterReceiver(mNetworkChangeListener)
        } catch (e: Exception) {
            Log.e(TAG, "onStop: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = false,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), "Exception Point" to "Main Activity : onStop", "Exception Message" to e.message, "Exception Logs" to e.toString())
            )
        }
        super.onStop()
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.labelVisibilityMode = LABEL_VISIBILITY_LABELED
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    fun checkBottomNavBarFeatureVisibility() {
        StaticInstances.sPermissionHashMap?.forEach { (key, value) ->
            runOnUiThread {
                Log.d(TAG, "$key = $value")
                if (Constants.PAGE_ORDER == key && !value) {
                    blurBottomNavBarContainer?.visibility = View.VISIBLE
                    blurBottomNavBarOrders?.visibility = View.VISIBLE
                } else if (Constants.PAGE_ORDER == key && value) blurBottomNavBarOrders?.visibility = View.INVISIBLE
                if (Constants.PAGE_CATALOG == key && !value) {
                    blurBottomNavBarContainer?.visibility = View.VISIBLE
                    blurBottomNavBarCatalog?.visibility = View.VISIBLE
                } else if (Constants.PAGE_CATALOG == key && value) blurBottomNavBarCatalog?.visibility = View.INVISIBLE
                if (Constants.PAGE_PREMIUM == key && !value) {
                    blurBottomNavBarContainer?.visibility = View.VISIBLE
                    blurBottomNavBarPremium?.visibility = View.VISIBLE
                    premiumImageView?.alpha = 0.25f
                } else if (Constants.PAGE_PREMIUM == key && value) {
                    premiumImageView?.alpha = 1f
                    blurBottomNavBarPremium?.visibility = View.INVISIBLE
                }
                if (Constants.PAGE_MARKETING == key && !value) {
                    blurBottomNavBarContainer?.visibility = View.VISIBLE
                    blurBottomNavBarMarketing?.visibility = View.VISIBLE
                } else if (Constants.PAGE_MARKETING == key && value) blurBottomNavBarMarketing?.visibility = View.INVISIBLE
                if (Constants.PAGE_SETTINGS == key && !value) {
                    blurBottomNavBarContainer?.visibility = View.VISIBLE
                    blurBottomNavBarSettings?.visibility = View.VISIBLE
                } else if (Constants.PAGE_SETTINGS == key && value) blurBottomNavBarSettings?.visibility = View.INVISIBLE
            }
        }
    }

    override fun onBackPressed() {
        try {
            val current: BaseFragment? = getCurrentFragment()
            if (current?.onBackPressed() == true) return
            val manager = supportFragmentManager
            if (manager.backStackEntryCount > 0) super.onBackPressed()
        } catch (e: Exception) {
            Log.e(TAG, "onBackPressed: ${e.message}", e)
        }
    }

    fun onClick(v: View?) {
        getCurrentFragment()?.onClick(v)
    }

    fun getCurrentFragment(): BaseFragment? {
        val mgr = supportFragmentManager
        val list = mgr.fragments
        val count = mgr.backStackEntryCount
        if (0 == count) {
            if (isNotEmpty(list)) {
                for (i in list.indices) {
                    if (list[i] is BaseFragment) {
                        return list[i] as BaseFragment
                    }
                }
            }
            return BaseFragment()
        }
        val entry = mgr.getBackStackEntryAt(count - 1)
        return try {
            mgr.findFragmentByTag(entry.name) as BaseFragment
        } catch (e: Exception) {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = false,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), "Exception Point" to "getCurrentFragment", "Exception Message" to e.message, "Exception Logs" to e.toString())
            )
            null
        }
    }

    fun showToast(message: String?) {
        runOnUiThread {
            try {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "showToast: ${e.message}", e)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        "Exception Point" to "showToast",
                        "Exception Message" to e.message,
                        "Exception Logs" to e.toString()
                    )
                )
            }
        }
    }

    fun launchFragment(fragment: Fragment?, addBackStack: Boolean) = runOnUiThread { doSwitchToScreen(fragment, addBackStack) }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        getCurrentFragment()?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun doSwitchToScreen(fragment: Fragment?, addToBackStack: Boolean) {
        if (null == fragment) return
        val manager = supportFragmentManager
        val fragmentTransaction = manager.beginTransaction()
        val fragmentTag = fragment.javaClass.canonicalName
        try {
            fragmentTransaction.setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            fragmentTransaction.replace(R.id.homeFrame, fragment, fragmentTag)
            if (addToBackStack) fragmentTransaction.addToBackStack(fragmentTag)
            fragmentTransaction.commitAllowingStateLoss()
        } catch (e: Exception) {
            Log.e("doSwitchToScreen ", e.message, e)
            try {
                fragmentTransaction.replace(R.id.homeFrame, fragment, fragmentTag)
                if (addToBackStack) fragmentTransaction.addToBackStack(fragmentTag)
                fragmentTransaction.commitAllowingStateLoss()
            } catch (e2: Exception) {
                Log.e("doSwitchToScreen", e.message, e)
            }
        }
    }

    fun launchFragmentWithAnimation(fragment: Fragment?, addBackStack: Boolean, animationView: View) = runOnUiThread { doSwitchToScreen(fragment, addBackStack, animationView) }

    private fun doSwitchToScreen(fragment: Fragment?, addToBackStack: Boolean, animationView: View) {
        if (null == fragment) {
            return
        }
        val manager = supportFragmentManager
        val fragmentTransaction = manager.beginTransaction()
        val fragmentTag = fragment.javaClass.canonicalName
        try {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
            fragmentTransaction.replace(R.id.homeFrame, fragment, fragmentTag)
            if (addToBackStack) fragmentTransaction.addToBackStack(fragmentTag)
            fragmentTransaction.addSharedElement(animationView, getString(R.string.transition_name))
            fragmentTransaction.commitAllowingStateLoss()
        } catch (e: Exception) {
            Log.e("doSwitchToScreen ", e.message, e)
            try {
                fragmentTransaction.replace(R.id.homeFrame, fragment, fragmentTag)
                if (addToBackStack) fragmentTransaction.addToBackStack(fragmentTag)
                fragmentTransaction.addSharedElement(animationView, getString(R.string.transition_name))
                fragmentTransaction.commitAllowingStateLoss()
            } catch (e2: Exception) {
                Log.e("doSwitchToScreen", e.message, e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getCurrentFragment()?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment = getCurrentFragment() ?: return false
        when (item.itemId) {
            R.id.menuHome -> {
                if (false == StaticInstances.sPermissionHashMap?.get(Constants.PAGE_ORDER)) {
                    getCurrentFragment()?.showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_ORDERS)
                    return true
                }
                if (fragment !is OrderFragment) launchFragment(OrderFragment.newInstance(), true)
            }
            R.id.menuSettings -> {
                if (false == StaticInstances.sPermissionHashMap?.get(Constants.PAGE_SETTINGS)) {
                    getCurrentFragment()?.showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_ORDERS)
                    return true
                }
                if (fragment !is SettingsFragment) launchFragment(SettingsFragment.newInstance(), true)
            }
            R.id.menuMarketing -> {
                if (false == StaticInstances.sPermissionHashMap?.get(Constants.PAGE_MARKETING)) {
                    getCurrentFragment()?.showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_MARKETING)
                    return true
                }
                if (fragment !is MarketingFragment) launchFragment(MarketingFragment.newInstance(), true)
            }
            R.id.menuProducts -> {
                if (false == StaticInstances.sPermissionHashMap?.get(Constants.PAGE_CATALOG)) {
                    getCurrentFragment()?.showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_CATALOG)
                    return true
                }
                if (fragment !is ProductFragment) launchFragment(ProductFragment.newInstance(), true)
            }
            R.id.menuPremium -> {
                if (false == StaticInstances.sPermissionHashMap?.get(Constants.PAGE_PREMIUM)) {
                    getCurrentFragment()?.showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_PREMIUM)
                    return true
                }
                if (fragment !is PremiumPageInfoFragment) {
                    AppEventsManager.pushAppEvents(eventName = AFInAppEventType.EVENT_PREMIUM_PAGE, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true, data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.CHANNEL to "isBottomNav"))
                    launchFragment(PremiumPageInfoFragment.newInstance(), true)
                }
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy :: called for application")
        StaticInstances.sAppSessionId = ""
        TruecallerSDK.clear()
    }

    override fun onNotificationClickedPayloadReceived(payload: HashMap<String, Any>?) {
        Log.d(TAG, "onNotificationClickedPayloadReceived: $payload")
    }

    override fun onInAppButtonClick(payload: HashMap<String, String>?) {
        Log.d(TAG, "onInAppButtonClick :: $payload")
    }

    private fun requestInAppReviewObject() {
        manager = ReviewManagerFactory.create(this)
        val request = manager?.requestReviewFlow()
        request?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
            }
        }
    }

    fun launchInAppReviewDialog() {
        reviewInfo?.let { info ->
            val flow = manager?.launchReviewFlow(this, info)
            flow?.addOnCompleteListener { _ ->
                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
            }
        }
    }
}