package com.digitaldukaan

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.InAppNotificationButtonListener
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener
import com.digitaldukaan.constants.*
import com.digitaldukaan.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_LABELED
import com.google.firebase.messaging.FirebaseMessaging
import com.truecaller.android.sdk.TruecallerSDK
import kotlinx.android.synthetic.main.activity_main2.*
import java.util.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    CTPushNotificationListener, InAppNotificationButtonListener {

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
        AppEventsManager.setAppEventsManager(this)
        if (StaticInstances.sAppSessionId?.isEmpty() == true) StaticInstances.sAppSessionId = RandomStringGenerator(16).nextString()
        if (PrefsManager.getStringDataFromSharedPref(PrefsManager.APP_INSTANCE_ID).isEmpty()) PrefsManager.storeStringDataInSharedPref(PrefsManager.APP_INSTANCE_ID, RandomStringGenerator(16).nextString())
        Log.d(TAG, "appSessionID :: ${StaticInstances.sAppSessionId}")
        Log.d(TAG, "appInstanceID :: ${PrefsManager.getStringDataFromSharedPref(PrefsManager.APP_INSTANCE_ID)}")
        val intentUri = intent?.data
        launchFragment(SplashFragment.newInstance(intentUri), true)
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(it.isComplete){
                StaticInstances.sFireBaseMessagingToken = it.result.toString()
                Log.d(TAG, "onCreate :: FIREBASE TOKEN :: ${it.result}")
                CleverTapAPI.getDefaultInstance(this)?.pushFcmRegistrationId(StaticInstances.sFireBaseMessagingToken, true)
            }
        }
        CleverTapAPI.getDefaultInstance(this)?.apply {
            setInAppNotificationButtonListener(this@MainActivity)
        }
    }

    override fun onStart() {
        val intentFiler = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(mNetworkChangeListener, intentFiler)
        super.onStart()
    }

    override fun onStop() {
        unregisterReceiver(mNetworkChangeListener)
        super.onStop()
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.labelVisibilityMode = LABEL_VISIBILITY_LABELED
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        val current: BaseFragment = getCurrentFragment()
        if (current.onBackPressed()) return
        val manager = supportFragmentManager
        if (manager.backStackEntryCount > 0) super.onBackPressed()
    }

    fun onClick(v: View?) {
        val fragment: BaseFragment = getCurrentFragment()
        fragment.onClick(v)
    }

    fun getCurrentFragment(): BaseFragment {
        val mgr = supportFragmentManager
        val list = mgr.fragments
        val count = mgr.backStackEntryCount
        if (0 == count) {
            if (list.isNotEmpty()) {
                for (i in list.indices) {
                    if (list[i] is BaseFragment) {
                        return list[i] as BaseFragment
                    }
                }
            }
            return BaseFragment()
        }
        val entry = mgr.getBackStackEntryAt(count - 1)
        return mgr.findFragmentByTag(entry.name) as BaseFragment
    }

    fun showToast(message: String?) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun launchFragment(fragment: Fragment?, addBackStack: Boolean) = runOnUiThread { doSwitchToScreen(fragment, addBackStack) }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        getCurrentFragment().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun doSwitchToScreen(fragment: Fragment?, addToBackStack: Boolean) {
        if (null == fragment) {
            return
        }
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
            fragmentTransaction.setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
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
        getCurrentFragment().onActivityResult(requestCode, resultCode, data)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuHome -> if (getCurrentFragment() !is HomeFragment) launchFragment(HomeFragment.newInstance(), true)
            R.id.menuSettings -> if (getCurrentFragment() !is SettingsFragment) launchFragment(SettingsFragment.newInstance(), true)
            R.id.menuMarketing -> if (getCurrentFragment() !is MarketingFragment) launchFragment(MarketingFragment.newInstance(), true)
            R.id.menuProducts -> if (getCurrentFragment() !is ProductFragment) launchFragment(ProductFragment.newInstance(), true)
            R.id.menuPremium -> if (getCurrentFragment() !is PremiumPageInfoFragment) launchFragment(PremiumPageInfoFragment.newInstance(), true)
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
}