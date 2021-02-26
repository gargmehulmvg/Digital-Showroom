package com.digitaldukaan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.fragments.SplashFragment
import kotlinx.android.synthetic.main.activity_main2.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ToolBarManager.getInstance().setupToolbar(toolbarLayout)
        launchFragment(SplashFragment(), true)
    }

    fun isGPSEnableForUser(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsProviderEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGpsProviderEnabled) {
            return true
        } else {
            AlertDialog.Builder(this).apply {
                title = "GPS Permission"
                setMessage("GPS is required for this app to work.\n Please enable GPS first")
                    .setPositiveButton("Allowed") { _, _ ->
                        run {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(intent, Constants.LOCATION_REQUEST_CODE)
                        }
                    }.setCancelable(false)
            }.show()
        }
        return false
    }

    fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Constants.LOCATION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Constants.LOCATION_REQUEST_CODE == requestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Sorry, App can't be proceed without granted permission",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
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

    private fun getCurrentFragment(): BaseFragment {
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

    private fun doSwitchToScreen(fragment: Fragment?, addToBackStack: Boolean) {
        if (null == fragment) {
            return
        }
        val manager = supportFragmentManager
        val fragmentTransaction = manager.beginTransaction()
        val fragmentTag = fragment.javaClass.canonicalName
        try {
            fragmentTransaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
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
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
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

}