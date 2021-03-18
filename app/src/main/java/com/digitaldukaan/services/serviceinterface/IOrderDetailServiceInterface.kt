package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IOrderDetailServiceInterface {

    fun onOrderDetailResponse(commonResponse: CommonApiResponse)

    fun onOrderDetailException(e: Exception)

}