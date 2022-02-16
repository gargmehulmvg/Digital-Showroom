package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class LeadDetailResponse(

	@field:SerializedName("items")
	val leadsDetailsItemsList: ArrayList<LeadsDetailItemResponse>?,

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
	val buyerPayValue: String? = "",

	@field:SerializedName("store_offer")
	val storeOffer: StoreOfferItemResponse?
)

data class StoreOfferItemResponse(

	@field:SerializedName("promo_id")
	val promoId: Int = 0,

	@field:SerializedName("promo_code")
	val promoCode: String? = "",

	@field:SerializedName("promo_discount")
	val promoDiscount: Double = 0.0
)

data class LeadsDetailItemResponse(

	@field:SerializedName("item_id")
	val item_id: String = "",

	@field:SerializedName("item_name")
	val itemName: String = "",

	@field:SerializedName("item_quantity")
	val item_quantity: String = "",

	@field:SerializedName("quantity")
	val quantity: String = "",

	@field:SerializedName("item_price")
	val item_price: String = "",

	@field:SerializedName("amount")
	val amount: String = "",

	@field:SerializedName("actual_price")
	val actual_price: String = "",

	@field:SerializedName("discounted_price")
	val discounted_price: String = "",

	@field:SerializedName("item_type")
	val item_type: String = "",

	@field:SerializedName("image_url")
	val imageUrl: String = "",

	@field:SerializedName("thumbnail_url")
	val thumbnail_url: String = "",

	@field:SerializedName("available")
	val available: String = "",

	@field:SerializedName("variants_selected")
	val leadsVariantList: ArrayList<LeadsVariantItemResponse>,

	@field:SerializedName("managed_inventory")
	val managed_inventory: String = ""
)

data class LeadsVariantItemResponse(
	@field:SerializedName("variant_id")
	val variantId: String = "",

	@field:SerializedName("variant_name")
	val variantName: String = "",

	@field:SerializedName("available")
	val available: String = "",

	@field:SerializedName("quantity")
	val quantity: String = "",

	@field:SerializedName("amount")
	val amount: String = "",

	@field:SerializedName("price")
	val price: String = "",

	@field:SerializedName("image_url")
	val imageUrl: String = "",

	@field:SerializedName("thumbnail_url")
	val thumbnailUrl: String = "",

	@field:SerializedName("discounted_price")
	val discountedPrice: String = "",

	@field:SerializedName("managed_inventory")
	val managedInventory: String = ""
)