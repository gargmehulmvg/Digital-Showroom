package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.StaticTextResponse

interface ISplashServiceInterface {

    fun onStaticDataResponse(staticDataResponse: StaticTextResponse)

    fun onStaticDataException(e: Exception)
}