package com.digitaldukaan.fragments

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.response.StaticTextResponse
import com.google.android.material.snackbar.Snackbar
import java.net.UnknownHostException


open class BaseFragment : Fragment() {

    protected lateinit var mContentView: View
    private var mProgressDialog: Dialog? = null
    protected lateinit var mActivity: MainActivity

    companion object {
        lateinit var mStaticData: StaticTextResponse
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as MainActivity
    }

    protected fun showProgressDialog(context: Context?, message: String? = "Please wait...") {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            context?.run {
                mProgressDialog = Dialog(this)
                val inflate = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
                mProgressDialog?.setContentView(inflate)
                mProgressDialog?.setCancelable(false)
                mProgressDialog?.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT)
                )
                mProgressDialog?.show()
            }
        }
    }

    open fun onClick(view: View?) {}

    open fun onBackPressed() : Boolean  = false

    protected fun showCancellableProgressDialog(context: Context?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            context?.run {
                mProgressDialog = Dialog(this)
                val inflate = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
                mProgressDialog?.setContentView(inflate)
                mProgressDialog?.setCancelable(true)
                mProgressDialog?.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT)
                )
                mProgressDialog?.show()
            }
        }
    }

    protected fun stopProgress() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (mProgressDialog != null) {
                mProgressDialog?.let {
                    mProgressDialog?.dismiss()
                }
            }
        }
    }

    protected fun showToast(message: String? = "sample testing") {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    open fun exceptionHandlingForAPIResponse(e: Exception) {
        stopProgress()
        if (e is UnknownHostException) {
            showToast(e.message)
        }
    }

    protected fun showShortSnackBar(message: String = "sample testing") {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            Snackbar.make(mContentView, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun EditText.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun EditText.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }

    open fun launchFragment(fragment: Fragment?, addBackStack: Boolean) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.launchFragment(fragment, addBackStack)
        }
    }

    open fun launchFragment(fragment: Fragment?, addBackStack: Boolean, animationView: View) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.launchFragmentWithAnimation(fragment, addBackStack, animationView)
        }
    }

    open fun clearFragmentBackStack() {
        val fm = mActivity.supportFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
    }

    open fun showNoInternetConnectionDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
            builder.apply {
                setTitle(getString(R.string.no_internet_connection))
                setMessage(getString(R.string.turn_on_internet_message))
                setCancelable(false)
                setNegativeButton(getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
    }

    open fun storeStringDataInSharedPref(keyName: String, value: String?) {
        val editor = mActivity.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE).edit()
        editor.putString(keyName, value)
        editor.apply()
    }

    open fun getStringDataFromSharedPref(keyName: String?): String {
        val prefs = mActivity.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)
        return prefs.getString(keyName, "").toString()
    }

    open fun openUrlInBrowser(url:String?) {
        url?.let {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    open fun shareDataOnWhatsApp(sharingData: String?) {
        val whatsAppIntent = Intent(Intent.ACTION_SEND)
        whatsAppIntent.type = "text/plain"
        whatsAppIntent.setPackage("com.whatsapp")
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, sharingData)
        try {
            mActivity.startActivity(whatsAppIntent)
        } catch (ex: ActivityNotFoundException) {
            showToast("Whatsapp have not been installed.")
        }
    }

    open fun openPlayStore() {
        val appPackageName: String = mActivity.packageName
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (ignore: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }
}