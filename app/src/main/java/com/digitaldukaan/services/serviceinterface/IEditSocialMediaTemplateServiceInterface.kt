package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IEditSocialMediaTemplateServiceInterface {

    fun onEditSocialMediaTemplateErrorResponse(e: Exception)

    fun onItemsBasicDetailsByStoreIdResponse(response: CommonApiResponse)

    fun onProductCategoryResponse(response: CommonApiResponse)

    fun onSocialMediaTemplateBackgroundsResponse(response: CommonApiResponse)

}