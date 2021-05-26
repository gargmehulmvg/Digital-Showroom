package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StoreDeliveryServiceResponse(
    @SerializedName("store_id") var mStoreId: Int,
    @SerializedName("store_flag") var mStoreFlag: Int,
    @SerializedName("delivery_flag") var mDeliveryFlag: Int,
    @SerializedName("pickup_flag") var mPickupFlag: Int,
    @SerializedName("listing_flag") var mListingFlag: Int,
    @SerializedName("min_order_value") var mMinOrderValue: Double,
    @SerializedName("delivery_charge_type") var mDeliveryChargeType: Int,
    @SerializedName("free_delivery_above") var mFreeDeliveryAbove: Double,
    @SerializedName("delivery_price") var mDeliveryPrice: Double
)