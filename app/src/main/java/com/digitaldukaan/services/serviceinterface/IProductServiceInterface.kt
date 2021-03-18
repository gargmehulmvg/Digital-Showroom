package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IProductServiceInterface {

    fun onProductResponse(commonResponse: CommonApiResponse)

    fun onProductException(e: Exception)

}