package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IBankDetailsServiceInterface {

    fun onBankDetailsResponse(response: CommonApiResponse)

    fun onBankDetailsPageInfoResponse(response: CommonApiResponse)

    fun onBankDetailsServerException(e: Exception)
}