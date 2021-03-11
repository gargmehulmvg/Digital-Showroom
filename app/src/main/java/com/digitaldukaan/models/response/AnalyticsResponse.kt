package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class AnalyticsResponse (
    @SerializedName("today") var today: TodayItemResponse?,
    @SerializedName("this_week") var thisWeek: ThisWeekItemResponse?,
    @SerializedName("static_text") var analyticsStaticData: AnalyticsStaticData?
)

data class AnalyticsStaticData(
    @SerializedName("text_rupee_symbol") var textRuppeeSymbol: String?,
    @SerializedName("text_today_sale") var textTodaySale: String?,
    @SerializedName("text_week_sale") var textWeekSale: String?,
    @SerializedName("text_today_amount") var textTodayAmount: String?,
    @SerializedName("text_week_amount") var textWeekAmount: String?
)

data class TodayItemResponse(
    @SerializedName("total_count") var totalCount: String?,
    @SerializedName("total_amount") var totalAmount: String?,
    @SerializedName("total_cash") var totalCash: String?,
    @SerializedName("total_online") var totalOnline: String?
)

data class ThisWeekItemResponse(
    @SerializedName("total_count") var totalCount: String?,
    @SerializedName("total_amount") var totalAmount: String?,
    @SerializedName("total_cash") var totalCash: String?,
    @SerializedName("total_online") var totalOnline: String?
)