package com.digitaldukaan.interfaces

interface IWebViewCallbacks {

    fun onNativeBackPressed()

    fun sendData(data: String)
}