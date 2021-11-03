package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class CustomDomainBottomSheetResponse(
    @SerializedName("primary_domain")       var primaryDomain: PrimaryDomainItemResponse?,
    @SerializedName("suggested_domains")    var suggestedDomainsList: ArrayList<PrimaryDomainItemResponse?>?,
    @SerializedName("search_cta")           var searchCta: CTAItemResponse?,
    @SerializedName("static_text")          var staticText: CustomDomainBottomSheetStaticTextResponse?
)

data class PrimaryDomainItemResponse(
    @SerializedName("heading")              var heading: String,
    @SerializedName("domain_name")          var domainName: String,
    @SerializedName("original_price")       var originalPrice: String,
    @SerializedName("renewal_price")        var renewalPrice: String,
    @SerializedName("discounted_price")     var discountedPrice: String,
    @SerializedName("promo")                var promo: String,
    @SerializedName("validity")             var validity: String,
    @SerializedName("cta")                  var cta: CustomDomainCtaResponse?,
    @SerializedName("info_data")            var infoData: CustomDomainInfoTextResponse?
)

data class CustomDomainCtaResponse(
    @SerializedName("text")                 var text: String,
    @SerializedName("text_color")           var textColor: String,
    @SerializedName("text_bg")              var textBg: String,
    @SerializedName("action")               var action: String,
    @SerializedName("page_url")             var pageUrl: String
)

data class CustomDomainInfoTextResponse(
    @SerializedName("first_year_text")      var firstYearText: String,
    @SerializedName("renews_text")          var renewsText: String
)

data class CustomDomainBottomSheetStaticTextResponse(
    @SerializedName("heading_last_step")                    var heading_last_step: String,
    @SerializedName("subheading_budiness_needs_domain")     var subheading_budiness_needs_domain: String,
    @SerializedName("text_cant_find")                       var text_cant_find: String,
    @SerializedName("text_more_suggestions")                var text_more_suggestions: String,
    @SerializedName("text_best_pick_for_you")               var text_best_pick_for_you: String,
    @SerializedName("text_search")                          var text_search: String
)