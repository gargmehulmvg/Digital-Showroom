package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StaffInvitationResponse(

    @field:SerializedName("cta")
    val cta: CommonCtaResponse? = null,

    @field:SerializedName("store_id")
    val storeId: Int,

    @field:SerializedName("invitation_list")
    val invitationList: ArrayList<InvitationListItem?>? = null,

    @field:SerializedName("text_more_options")
    val textMoreOptions: String? = null,

    @field:SerializedName("heading")
    val heading: String? = null,

    @field:SerializedName("cdn")
    val cdn: String? = null
)
