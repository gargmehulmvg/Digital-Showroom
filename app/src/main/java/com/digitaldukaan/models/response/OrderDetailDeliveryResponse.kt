package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class OrderDetailDeliveryResponse(

	@field:SerializedName("is_enabled")
    val isEnabled: Boolean = false,

	@field:SerializedName("delivery_status_sub_heading")
    val deliveryStatusSubHeading: String? = null,

	@field:SerializedName("cta")
    val cta: OrderDetailDeliveryCta? = null,

	@field:SerializedName("is_order_serviceable")
    val isOrderServiceable: Boolean = false,

	@field:SerializedName("inner_bg_color")
    val innerBgColor: String? = null,

	@field:SerializedName("outer_bg_color")
    val outerBgColor: String? = null,

	@field:SerializedName("text_share")
    val textShare: String? = null,

	@field:SerializedName("delivery_partner_image_url")
    val deliveryPartnerImageUrl: String? = null,

	@field:SerializedName("delivery_schedule_pickup_url")
    val deliverySchedulePickupUrl: String? = null,

	@field:SerializedName("delivery_display_status")
    val deliveryDisplayStatus: String? = null,

	@field:SerializedName("share_wa_message")
    val shareWaMessage: String? = null
)

data class OrderDetailDeliveryCta(

    @field:SerializedName("is_enabled")
    val isEnabled: Boolean = false,

    @field:SerializedName("bg_color")
    val bgColor: String? = null,

    @field:SerializedName("action")
    val action: String? = null,

    @field:SerializedName("text")
    val text: String? = null,

    @field:SerializedName("text_color")
    val textColor: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("cdn")
    val cdn: String? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("message_tooltip")
    val messageTooltip: String? = null
)
