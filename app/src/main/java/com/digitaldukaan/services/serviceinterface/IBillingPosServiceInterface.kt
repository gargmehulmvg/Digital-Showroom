package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IBillingPosServiceInterface {

    fun onBillingPosPageInfoResponse(response: CommonApiResponse)

    fun onRequestCallBackResponse(response: CommonApiResponse)

    fun onBillingPosServerException(e: Exception)
}