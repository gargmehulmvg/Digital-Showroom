package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class SearchCatalogItemsRequest(
    @SerializedName("page")         var pageNumber: Int,
    @SerializedName("search_text")  var searchText: String?
)