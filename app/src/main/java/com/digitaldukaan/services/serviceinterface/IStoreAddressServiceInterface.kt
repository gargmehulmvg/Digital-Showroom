package com.digitaldukaan.services.serviceinterface

import okhttp3.ResponseBody

interface IStoreAddressServiceInterface {


    fun onStoreAddressResponse(response: ResponseBody)

    fun onStoreAddressServerException(e: Exception)
}