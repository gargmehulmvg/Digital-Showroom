package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class LeadDetailResponse(

	@field:SerializedName("items")
	val orderDetailsItemsList: ArrayList<OrderDetailItemResponse>?,

	@field:SerializedName("static_text")
	val staticText: LeadDetailStaticTextResponse? = null,

	@field:SerializedName("order_type")
	val orderType: Int = 0,

	@field:SerializedName("amount")
	val amount: Double = 0.0,

	@field:SerializedName("discount")
	val discount: Double = 0.0,

	@field:SerializedName("extra_charges")
	val extraCharges: Double = 0.0,

	@field:SerializedName("extra_charge_name")
	val extraChargeName: String = "",

	@field:SerializedName("delivery_charge")
	val deliveryCharge: Double = 0.0,

	@field:SerializedName("pay_amount")
	val payAmount: Double = 0.0,

	@field:SerializedName("instructions")
	val instructions: String = "",

	@field:SerializedName("delivery_info")
	val deliveryInfo: DeliveryInfoItemResponse?,

	@field:SerializedName("store_id")
	val storeId: String? = "",
	
	@field:SerializedName("cart_id")
	val cartId: String? = "",
	
	@field:SerializedName("user_phone")
	val userPhone: String? = "",
	
	@field:SerializedName("cart_type")
	val cartType: Int,
	
	@field:SerializedName("items_total")
	val itemsTotal: String? = "",
	
	@field:SerializedName("prepaid_flag")
	val prepaidFlag: String? = "",
	
	@field:SerializedName("source")
	val source: String? = "",
	
	@field:SerializedName("send_reminder_flag")
	val sendReminderFlag: Boolean,
	
	@field:SerializedName("buyer_pay_value")
	val buyerPayValue: String? = ""
)