package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.BusinessTypeResponse

interface IBusinessTypeServiceInterface {

    fun onBusinessTypeResponse(response: BusinessTypeResponse)

    fun onBusinessTypeServerException(e: Exception)
}