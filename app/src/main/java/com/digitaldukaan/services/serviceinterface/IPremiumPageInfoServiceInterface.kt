package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IPremiumPageInfoServiceInterface {

    fun onPremiumPageInfoResponse(response: CommonApiResponse)

    fun onPremiumPageInfoServerException(e: Exception)
}