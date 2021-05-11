package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IEditPremiumServiceInterface {

    fun onPremiumColorsResponse(response: CommonApiResponse)

    fun onSetPremiumColorsResponse(response: CommonApiResponse)

    fun onEditPremiumServerException(e: Exception)
}