package com.digitaldukaan

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.digitaldukaan.interfaces.IOnBackPressedListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbarView)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mNavController = (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController
        setupActionBarWithNavController(mNavController)
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)?.childFragmentManager?.fragments?.get(0)
        if (fragment is IOnBackPressedListener) {
            (fragment as IOnBackPressedListener).onBackPressedToExit()
        } else if (!mNavController.popBackStack()) {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return mNavController.navigateUp() || super.onSupportNavigateUp()
    }

    fun hideToolbarView(isToolbarVisible: Boolean) {
        if (isToolbarVisible)
            toolbarView.visibility = View.VISIBLE
        else {
            toolbarView.visibility = View.GONE
        }
    }
}