package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IOrderDetailServiceInterface {

    fun onOrderDetailResponse(commonResponse: CommonApiResponse)

    fun onDeliveryTimeResponse(commonResponse: CommonApiResponse)

    fun onUpdateOrderResponse(commonResponse: CommonApiResponse)

    fun onCompleteOrderStatusResponse(commonResponse: CommonApiResponse)

    fun onShareBillResponse(commonResponse: CommonApiResponse)

    fun onSharePaymentLinkResponse(commonResponse: CommonApiResponse)

    fun onOrderDetailStatusResponse(commonResponse: CommonApiResponse)

    fun onUpdateStatusResponse(commonResponse: CommonApiResponse)

    fun onPrepaidOrderUpdateStatusResponse(commonResponse: CommonApiResponse)

    fun onOrderDetailException(e: Exception)

}