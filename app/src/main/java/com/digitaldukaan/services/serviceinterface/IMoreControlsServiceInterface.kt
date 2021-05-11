package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IMoreControlsServiceInterface {

    fun onMoreControlsResponse(response: CommonApiResponse)

    fun onMoreControlsServerException(e: Exception)
}