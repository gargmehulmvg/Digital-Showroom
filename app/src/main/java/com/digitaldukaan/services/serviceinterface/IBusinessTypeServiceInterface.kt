package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.BusinessTypeResponse
import com.digitaldukaan.models.response.StoreDescriptionResponse

interface IBusinessTypeServiceInterface {

    fun onBusinessTypeResponse(response: BusinessTypeResponse)

    fun onSavingBusinessTypeResponse(response: StoreDescriptionResponse)

    fun onBusinessTypeServerException(e: Exception)
}