package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class InvitationListItem(

    @field:SerializedName("heading")
    val heading: String? = null,

    @field:SerializedName("sub_heading")
    val subHeading: String? = null,

    @field:SerializedName("id")
    val id: Int,

    var isSelected: Boolean = false
)