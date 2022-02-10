package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface ILeadsDetailServiceInterface {

    fun onLeadsDetailException(e: Exception)

    fun onGetOrderCartByIdResponse(commonResponse: CommonApiResponse)

    fun onSendAbandonedCartReminderResponse(commonResponse: CommonApiResponse)

}