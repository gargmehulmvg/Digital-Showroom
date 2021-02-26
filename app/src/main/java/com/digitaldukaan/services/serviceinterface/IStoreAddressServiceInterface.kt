package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.StoreAddressResponse

interface IStoreAddressServiceInterface {


    fun onStoreAddressResponse(response: StoreAddressResponse)

    fun onStoreAddressServerException(e: Exception)
}