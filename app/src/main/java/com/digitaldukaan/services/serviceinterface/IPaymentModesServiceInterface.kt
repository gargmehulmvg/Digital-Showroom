package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IPaymentModesServiceInterface {

    fun onPaymentModesPageInfoResponse(response: CommonApiResponse)

    fun onSetPaymentModesResponse(response: CommonApiResponse)

    fun onPaymentModesServerException(e: Exception)
}