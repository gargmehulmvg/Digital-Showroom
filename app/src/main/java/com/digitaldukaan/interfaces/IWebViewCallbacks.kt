package com.digitaldukaan.interfaces

interface IWebViewCallbacks {

    fun onNativeBackPressed()

    fun sendData(data: String)

    fun showAndroidToast(data: String)

    fun showAndroidLog(data: String)
}