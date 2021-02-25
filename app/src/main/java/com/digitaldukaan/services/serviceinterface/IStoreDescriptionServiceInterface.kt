package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.StoreDescriptionResponse

interface IStoreDescriptionServiceInterface {

    fun onStoreDescriptionResponse(response: StoreDescriptionResponse)

    fun onStoreDescriptionServerException(e: Exception)
}