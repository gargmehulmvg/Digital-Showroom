package com.digitaldukaan.fragments

import com.digitaldukaan.interfaces.IWebViewCallbacks

open class BaseChildFragment : BaseFragment(), IWebViewCallbacks {

    override fun onNativeBackPressed() {
    }
}