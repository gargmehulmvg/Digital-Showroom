package com.digitaldukaan.services.serviceinterface

import okhttp3.ResponseBody

interface IStoreDescriptionServiceInterface {

    fun onStoreDescriptionResponse(response: ResponseBody)

    fun onStoreDescriptionServerException(e: Exception)
}