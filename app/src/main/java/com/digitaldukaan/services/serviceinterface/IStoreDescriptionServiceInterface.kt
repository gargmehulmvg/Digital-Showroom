package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IStoreDescriptionServiceInterface {

    fun onStoreDescriptionResponse(response: CommonApiResponse)

    fun onStoreDescriptionServerException(e: Exception)
}