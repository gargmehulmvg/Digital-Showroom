package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IMyPaymentsServiceInterface {

    fun onMyPaymentsListResponse(response: CommonApiResponse)

    fun onMyPaymentsServerException(e: Exception)
}