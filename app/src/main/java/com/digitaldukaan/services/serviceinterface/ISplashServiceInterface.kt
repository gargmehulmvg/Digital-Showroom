package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.StaticTextResponse

interface ISplashServiceInterface {

    fun onStaticDataResponse(staticDataResponse: StaticTextResponse)

    fun onAppVersionResponse(commonResponse: CommonApiResponse)

    fun onStaticDataException(e: Exception)
}