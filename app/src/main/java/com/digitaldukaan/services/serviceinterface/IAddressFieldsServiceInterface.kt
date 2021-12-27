package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IAddressFieldsServiceInterface {

    fun onAddressFieldsPageInfoResponse(response: CommonApiResponse)

    fun onAddressFieldsServerException(e: Exception)
}