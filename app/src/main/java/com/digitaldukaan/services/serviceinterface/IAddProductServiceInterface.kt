package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IAddProductServiceInterface {

    fun onAddProductBannerStaticDataResponse(commonResponse: CommonApiResponse)

    fun onAddProductException(e: Exception)

}