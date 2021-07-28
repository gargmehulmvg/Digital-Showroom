package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IPromoCodePageInfoServiceInterface {

    fun onPromoCodePageInfoException(e: Exception)

    fun onPromoCodePageInfoResponse(response: CommonApiResponse)

    fun onPromoCodeDetailResponse(response: CommonApiResponse)

    fun onGetPromoCodeListResponse(response: CommonApiResponse)

}