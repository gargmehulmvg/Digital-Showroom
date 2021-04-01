package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ExploreCategoryItemResponse

interface IExploreCategoryServiceInterface {

    fun onExploreCategoryResponse(response: CommonApiResponse)

    fun onExploreCategoryItemClickedResponse(response: ExploreCategoryItemResponse?)

    fun onExploreCategoryServerException(e: Exception)
}