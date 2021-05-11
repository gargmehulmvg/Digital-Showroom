package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ExploreCategoryItemResponse
import com.digitaldukaan.models.response.MasterCatalogItemResponse

interface IExploreCategoryServiceInterface {

    fun onExploreCategoryResponse(response: CommonApiResponse)

    fun onBuildCatalogResponse(response: CommonApiResponse)

    fun onSubCategoryItemsResponse(response: CommonApiResponse)

    fun onCategoryItemsClickResponse(response: MasterCatalogItemResponse?)

    fun onCategoryItemsImageClick(response: MasterCatalogItemResponse?)

    fun onCategoryItemsSetPriceClick(position: Int, response: MasterCatalogItemResponse?)

    fun onCategoryCheckBoxClick(position: Int, response: MasterCatalogItemResponse?, isChecked: Boolean)

    fun onExploreCategoryItemClick(response: ExploreCategoryItemResponse?)

    fun onExploreCategoryServerException(e: Exception)
}