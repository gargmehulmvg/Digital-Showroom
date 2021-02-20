package com.digitaldukaan

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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

}