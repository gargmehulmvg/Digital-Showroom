package com.digitaldukaan.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.google.android.material.snackbar.Snackbar
import java.net.UnknownHostException


open class BaseFragment : Fragment() {

    protected lateinit var mContentView: View
    private lateinit var mProgressDialog: Dialog
    protected lateinit var mActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as MainActivity
    }

    protected fun showProgressDialog(context: Context?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            context?.run {
                mProgressDialog = Dialog(this)
                val inflate = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
                mProgressDialog.setContentView(inflate)
                mProgressDialog.setCancelable(false)
                mProgressDialog.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT)
                )
                mProgressDialog.show()
            }
        }
    }

    open fun onClick(view: View?) {

    }

    open fun onBackPressed() : Boolean {
        return false;
    }

    protected fun showCancellableProgressDialog(context: Context?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            context?.run {
                mProgressDialog = Dialog(this)
                val inflate = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
                mProgressDialog.setContentView(inflate)
                mProgressDialog.setCancelable(true)
                mProgressDialog.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT)
                )
                mProgressDialog.show()
            }
        }
    }

    protected fun stopProgress() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mProgressDialog.run {
                mProgressDialog.dismiss()
            }
        }
    }

    protected fun showToast(message: String? = "sample testing") {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    open fun exceptionHandlingForAPIResponse(e: Exception) {
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

    open fun launchFragment(fragment: Fragment?, addBackStack: Boolean) = mActivity.launchFragment(fragment, addBackStack)

    open fun launchFragment(fragment: Fragment?, addBackStack: Boolean, animationView: View) = mActivity.launchFragmentWithAnimation(fragment, addBackStack, animationView)

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
                setTitle("No Internet Connection")
                setMessage("Please turn on internet connection to continue")
                setCancelable(false)
                setNegativeButton("Close") {dialog, _ -> dialog.dismiss() }
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
    }

}