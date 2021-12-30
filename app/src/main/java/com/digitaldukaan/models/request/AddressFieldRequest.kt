package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class AddressFieldRequest(
    @SerializedName("address_fields_list")      var addressFieldsList: ArrayList<AddressFieldItemRequest?>?
)

data class AddressFieldItemRequest(
    @SerializedName("id")                       var id: Int,
    @SerializedName("is_mandatory")             var isMandatory: Boolean,
    @SerializedName("is_field_selected")        var isFieldSelected: Boolean
)