package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IAppLocationServiceInterface {

    fun onGetStoreLocationResponse(commonApiResponse: CommonApiResponse)

    fun onGetStoreLocationException(e: Exception)
}