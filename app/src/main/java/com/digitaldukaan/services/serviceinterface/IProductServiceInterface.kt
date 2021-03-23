package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IProductServiceInterface {

    fun onProductResponse(commonResponse: CommonApiResponse)

    fun onProductShareStorePDFDataResponse(commonResponse: CommonApiResponse)

    fun onProductPDFGenerateResponse(commonResponse: CommonApiResponse)

    fun onProductShareStoreWAResponse(commonResponse: CommonApiResponse)

    fun onProductException(e: Exception)

}