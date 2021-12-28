package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class BillingPosPageInfoResponse (
    @SerializedName("main_features_list")       var mainFeaturesList: ArrayList<BillingPosFeatureListItemResponse?>?,
    @SerializedName("other_features_list")      var otherFeaturesList: ArrayList<String>?,
    @SerializedName("cta")                      var cta: CommonCtaResponse,
    @SerializedName("static_text")              var staticText: BillingPosStaticTextResponse?
)

data class BillingPosStaticTextResponse(
    @SerializedName("heading_main_features")    var heading_main_features: String?,
    @SerializedName("heading_other_features")   var heading_other_features: String?,
    @SerializedName("heading_page")             var heading_page: String?,
    @SerializedName("sub_heading_page")         var sub_heading_page: String?
)

data class BillingPosFeatureListItemResponse(
    @SerializedName("url")                      var url: String?,
    @SerializedName("text")                     var text: String?
)