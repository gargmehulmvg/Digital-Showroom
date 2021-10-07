package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class OrderDetailPrepaidDeliveryResponse(

    @field:SerializedName("is_enabled")
    var isEnabled: Boolean = false,

	@field:SerializedName("delivery_partner_image_url")
    val deliveryPartnerImageUrl: String? = null,

    @field:SerializedName("cta2")
    val cta2: OrderDetailDeliveryCta? = null,

	@field:SerializedName("delivery_display_status")
    val deliveryDisplayStatus: String? = null,

    @field:SerializedName("cta1")
    val cta1: OrderDetailDeliveryCta? = null,

    @field:SerializedName("delivery_status_sub_heading")
    val deliveryStatusSubHeading: String? = null,

	@field:SerializedName("message_goto_website")
    val messageGotoWebsite: String? = null,

	@field:SerializedName("inner_bg_color")
    val innerBgColor: String? = null,

	@field:SerializedName("outer_bg_color")
    val outerBgColor: String? = null,

	@field:SerializedName("heading_bottom_sheet")
    val headingBottomSheet: String? = null
)

