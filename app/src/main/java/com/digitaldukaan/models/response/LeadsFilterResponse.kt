package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class LeadsFilterResponse(
    @SerializedName("filter_list")              var filterList: ArrayList<LeadsFilterListItemResponse>,
    @SerializedName("static_text")              var staticText: LeadsFilterStaticTextResponse
)

data class LeadsFilterListItemResponse(
    @SerializedName("heading")                  var heading: String = "",
    @SerializedName("type")                     var type: String = "",
    @SerializedName("filter_options")           var filterOptionsList: ArrayList<LeadsFilterOptionsItemResponse>
)

data class LeadsFilterOptionsItemResponse(
    @SerializedName("id")                       var id: String = "",
    @SerializedName("text")                     var text: String = ""
)

data class LeadsFilterStaticTextResponse(
    @SerializedName("text_clear_filter")        var text_clear_filter: String = "",
    @SerializedName("text_done")                var text_done: String
)