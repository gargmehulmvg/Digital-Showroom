package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface ISendBillPhotoServiceInterface {

    fun onConvertFileToLinkResponse(commonResponse: CommonApiResponse)

    fun onUpdateOrderResponse(commonResponse: CommonApiResponse)

    fun onSendBillPhotoException(e: Exception)

}