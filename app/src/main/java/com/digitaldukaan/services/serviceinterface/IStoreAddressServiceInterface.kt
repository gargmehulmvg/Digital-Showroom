package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IStoreAddressServiceInterface {

    fun onStoreAddressResponse(response: CommonApiResponse)

    fun onGetStoreLocationResponse(commonApiResponse: CommonApiResponse)

    fun onStoreAddressServerException(e: Exception)
}