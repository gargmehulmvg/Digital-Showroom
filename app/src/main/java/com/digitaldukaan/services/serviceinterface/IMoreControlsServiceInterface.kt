package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.MoreControlsResponse

interface IMoreControlsServiceInterface {

    fun onMoreControlsResponse(response: MoreControlsResponse)

    fun onMoreControlsServerException(e: Exception)
}