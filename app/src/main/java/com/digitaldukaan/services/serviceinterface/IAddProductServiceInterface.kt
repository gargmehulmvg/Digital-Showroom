package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IAddProductServiceInterface {

    fun onAddProductBannerStaticDataResponse(commonResponse: CommonApiResponse)

    fun onGetAddProductDataResponse(commonResponse: CommonApiResponse)

    fun onAddProductDataResponse(commonResponse: CommonApiResponse)

    fun onConvertFileToLinkResponse(commonResponse: CommonApiResponse)

    fun onDeleteItemResponse(commonResponse: CommonApiResponse)

    fun onAddProductException(e: Exception)

}