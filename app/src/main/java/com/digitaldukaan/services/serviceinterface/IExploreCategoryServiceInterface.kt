package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ExploreCategoryItemResponse
import com.digitaldukaan.models.response.MasterCatalogItemResponse

interface IExploreCategoryServiceInterface {

    fun onExploreCategoryResponse(response: CommonApiResponse)

    fun onSubCategoryItemsResponse(response: CommonApiResponse)

    fun onCategoryItemsClickResponse(response: MasterCatalogItemResponse?)

    fun onCategoryItemsImageClickResponse(response: MasterCatalogItemResponse?)

    fun onExploreCategoryItemClickedResponse(response: ExploreCategoryItemResponse?)

    fun onExploreCategoryServerException(e: Exception)
}