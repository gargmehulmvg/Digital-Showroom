package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class WebConsoleBottomSheetResponse(
    @SerializedName("heading")                  var heading: String,
    @SerializedName("sub_heading")              var subHeading: String,
    @SerializedName("primary_cdn")              var primaryCdn: String,
    @SerializedName("secondary_cdn")            var secondaryCdn: String,
    @SerializedName("text_best_viewed_on")      var textBestViewedOn: String,
    @SerializedName("cta")                      var cta: CommonCtaResponse
)