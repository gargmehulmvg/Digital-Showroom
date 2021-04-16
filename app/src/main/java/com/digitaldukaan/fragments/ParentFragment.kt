package com.digitaldukaan.fragments

import androidx.fragment.app.Fragment
import com.digitaldukaan.interfaces.IWebViewCallbacks

open class ParentFragment : Fragment(), IWebViewCallbacks {

    override fun onNativeBackPressed() = Unit
    override fun sendData(data: String) = Unit
    override fun showAndroidToast(data: String) = Unit
}