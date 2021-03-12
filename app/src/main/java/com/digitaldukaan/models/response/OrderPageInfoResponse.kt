package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class OrderPageInfoResponse(
    @SerializedName("zero_order")               var mIsZeroOrder: ZeroOrderPageResponse,
    @SerializedName("is_take_order_on")         var mIsTakeOrder: Boolean,
    @SerializedName("is_search_order")          var mIsSearchOrder: Boolean,
    @SerializedName("help_page")                var mIsHelpOrder: HelpPageResponse,
    @SerializedName("banners")                  var mBannerList: ArrayList<HomePageBannerResponse>,
    @SerializedName("is_analytics_order")       var mIsAnalyticsOrder: Boolean,
    @SerializedName("completed_order_count")    var mCompletedOrderCount: Int,
    @SerializedName("pending_order_count")      var mPendingOrderCount: Int,
    @SerializedName("static_text")              var mHomePageStaticText: HomePageStaticTextResponse?
)

data class ZeroOrderPageResponse(
    @SerializedName("is_active")                var mIsActive: Boolean,
    @SerializedName("deep_link")                var mUrl: String
)

data class HelpPageResponse(
    @SerializedName("is_active")                var mIsActive: Boolean,
    @SerializedName("deep_link")                var mUrl: String
)

data class HomePageBannerResponse(
    @SerializedName("banner_url")               var mBannerUrl: String,
    @SerializedName("deep_link")                var mUrl: String
)

data class HomePageStaticTextResponse(
    @SerializedName("fetching_orders")          var fetching_orders: String,
    @SerializedName("heading_order_page")       var heading_order_page: String,
    @SerializedName("text_pending")             var text_pending: String,
    @SerializedName("text_completed")           var text_completed: String,
    @SerializedName("text_today")               var text_today: String
)