package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface ILeadsSearchServiceInterface {

    fun onLeadsSearchException(e: Exception)

    fun onGetCartsByFiltersResponse(commonResponse: CommonApiResponse)

}