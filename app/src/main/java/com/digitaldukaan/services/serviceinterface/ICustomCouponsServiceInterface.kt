package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface ICustomCouponsServiceInterface {

    fun onCustomCouponsErrorResponse(e: Exception)

    fun onCustomCouponsResponse(response: CommonApiResponse)

}