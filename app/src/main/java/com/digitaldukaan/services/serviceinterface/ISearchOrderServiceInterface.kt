package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface ISearchOrderServiceInterface {

    fun onSearchOrderResponse(commonResponse: CommonApiResponse)

    fun onSearchOrderException(e: Exception)

}