package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class DeliveryTimeResponse(
    @SerializedName("static_text") var staticText: DeliveryTimeStaticTextResponse?,
    @SerializedName("delivery_time_list") var deliveryTimeList: ArrayList<DeliveryTimeItemResponse>?
)

data class DeliveryTimeStaticTextResponse(
    @SerializedName("error_mandatory_field") var error_mandatory_field: String?,
    @SerializedName("hint_enter_delivery_time") var hint_enter_delivery_time: String?,
    @SerializedName("text_send_bill") var text_send_bill: String?,
    @SerializedName("heading_choose_delivery_time") var heading_choose_delivery_time: String?
)

data class DeliveryTimeItemResponse(
    @SerializedName("key") var key: String?,
    @SerializedName("value") var value: String?,
    var isSelected: Boolean
)