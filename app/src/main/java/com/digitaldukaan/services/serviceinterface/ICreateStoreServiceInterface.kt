package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface ICreateStoreServiceInterface {

    fun onCreateStoreResponse(commonApiResponse: CommonApiResponse)

    fun onCreateStoreServerException(e: Exception)

}