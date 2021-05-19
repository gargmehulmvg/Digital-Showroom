package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StoreResponse (
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("spotlight_flag") var spotlightFlag: Int,
    @SerializedName("score") var score: Int,
    @SerializedName("store_info") var storeInfo: UserStoreInfoResponse,
    @SerializedName("address") var storeAddress: UserAddressResponse?,
    @SerializedName("owner") var storeOwner: StoreOwnerResponse?,
    @SerializedName("bank_details") var bankDetails: BankDetailsResponse?,
    @SerializedName("store_businesses") var storeBusiness: ArrayList<StoreBusinessResponse>?,
    @SerializedName("services") var storeServices: StoreServicesResponse
)