package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IBusinessTypeServiceInterface {

    fun onBusinessTypeResponse(response: CommonApiResponse)

    fun onSavingBusinessTypeResponse(response: CommonApiResponse)

    fun onBusinessTypeServerException(e: Exception)
}