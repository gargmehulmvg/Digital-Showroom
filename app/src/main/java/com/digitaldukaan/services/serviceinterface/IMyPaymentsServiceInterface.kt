package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IMyPaymentsServiceInterface {

    fun onGetTransactionsListResponse(response: CommonApiResponse)

    fun onGetMyPaymentPageInfoResponse(response: CommonApiResponse)

    fun onMyPaymentsServerException(e: Exception)
}