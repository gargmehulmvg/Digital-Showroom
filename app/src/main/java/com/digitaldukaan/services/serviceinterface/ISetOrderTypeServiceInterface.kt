package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface ISetOrderTypeServiceInterface {

    fun onSetOrderTypeResponse(response: CommonApiResponse)

    fun onUpdatePaymentMethodResponse(response: CommonApiResponse)

    fun onSetOrderTypeException(e: Exception)
}