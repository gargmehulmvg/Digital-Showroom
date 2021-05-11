package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface ISplashServiceInterface {

    fun onStaticDataResponse(staticDataResponse: CommonApiResponse)

    fun onHelpScreenResponse(commonResponse: CommonApiResponse)

    fun onAppVersionResponse(commonResponse: CommonApiResponse)

    fun onStaticDataException(e: Exception)
}