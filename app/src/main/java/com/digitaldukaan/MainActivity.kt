package com.digitaldukaan

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.digitaldukaan.constants.*
import com.digitaldukaan.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
import com.truecaller.android.sdk.*
import kotlinx.android.synthetic.main.activity_main2.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
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
        initializeTrueCaller()
        val intentUri = intent?.data
        if (intentUri == null) launchFragment(SplashFragment.newInstance(), true) else switchToFragmentByDeepLink(intentUri)
    }

    private fun initializeTrueCaller() {
        /*val trueScope = TruecallerSdkScope.Builder(this, sdkCallback)
            .consentMode(TruecallerSdkScope.CONSENT_MODE_BOTTOMSHEET)
            .buttonColor(ContextCompat.getColor(this, R.color.black))
            .buttonTextColor(ContextCompat.getColor(this, R.color.white))
            .loginTextPrefix(TruecallerSdkScope.LOGIN_TEXT_PREFIX_TO_GET_STARTED)
            .loginTextSuffix(TruecallerSdkScope.LOGIN_TEXT_SUFFIX_PLEASE_VERIFY_MOBILE_NO)
            .ctaTextPrefix(TruecallerSdkScope.CTA_TEXT_PREFIX_USE)
            .buttonShapeOptions(TruecallerSdkScope.BUTTON_SHAPE_ROUNDED)
            //.privacyPolicyUrl("<<YOUR_PRIVACY_POLICY_LINK>>")
            //.termsOfServiceUrl("<<YOUR_PRIVACY_POLICY_LINK>>")
            .footerType(TruecallerSdkScope.FOOTER_TYPE_NONE)
            .consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_LOG_IN)
            .sdkOptions(TruecallerSdkScope.SDK_OPTION_WITHOUT_OTP)
            .build()*/
        val trueScope = TruecallerSdkScope.Builder(this, sdkCallback)
            .consentMode(TruecallerSdkScope.CONSENT_MODE_BOTTOMSHEET)
            .loginTextPrefix(TruecallerSdkScope.LOGIN_TEXT_PREFIX_TO_GET_STARTED)
            .loginTextSuffix(TruecallerSdkScope.LOGIN_TEXT_SUFFIX_PLEASE_VERIFY_MOBILE_NO)
            .buttonColor(ContextCompat.getColor(this, R.color.black))
            .buttonTextColor(ContextCompat.getColor(this, R.color.white))
            .consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_VERIFY)
            .footerType(TruecallerSdkScope.FOOTER_TYPE_NONE)
            .sdkOptions(TruecallerSdkScope.SDK_OPTION_WITHOUT_OTP)
            .buttonShapeOptions(TruecallerSdkScope.BUTTON_SHAPE_ROUNDED)
            .build()
        TruecallerSDK.init(trueScope)
    }

    private val sdkCallback = object : ITrueCallback {

        override fun onFailureProfileShared(p0: TrueError) {
            Log.d(TAG, "onFailureProfileShared: $p0")
        }

        override fun onSuccessProfileShared(p0: TrueProfile) {
            Log.d(TAG, "onSuccessProfileShared: $p0")
        }

        override fun onVerificationRequired(p0: TrueError?) {
            Log.d(TAG, "onVerificationRequired: $p0")
        }
    }

    private fun switchToFragmentByDeepLink(intentUri: Uri) {
        Toast.makeText(applicationContext, intentUri.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.labelVisibilityMode = LABEL_VISIBILITY_UNLABELED
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
        if (requestCode == TruecallerSDK.SHARE_PROFILE_REQUEST_CODE) {
            TruecallerSDK.getInstance().onActivityResultObtained(this, requestCode, resultCode, data);
        } else getCurrentFragment().onActivityResult(requestCode, resultCode, data)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuHome -> if (getCurrentFragment() !is HomeFragment) launchFragment(HomeFragment.newInstance(), true)
            R.id.menuSettings -> if (getCurrentFragment() !is SettingsFragment) launchFragment(SettingsFragment.newInstance(), true)
            R.id.menuMarketing -> if (getCurrentFragment() !is MarketingFragment) launchFragment(MarketingFragment.newInstance(), true)
            R.id.menuProducts -> if (getCurrentFragment() !is ProductFragment) launchFragment(ProductFragment.newInstance(), true)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy :: called for application")
        StaticInstances.sAppSessionId = ""
        TruecallerSDK.clear()
    }
}