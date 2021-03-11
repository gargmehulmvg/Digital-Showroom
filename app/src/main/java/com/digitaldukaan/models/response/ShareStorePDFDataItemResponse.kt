package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ShareStorePDFDataItemResponse(
    @SerializedName("heading") var heading: String,
    @SerializedName("sub_heading") var subHeading: String?,
    @SerializedName("image_url") var imageUrl: String?,
    @SerializedName("creating_pdf") var creatingPdf: String?,
    @SerializedName("whats_app_text") var whatsAppText: String?,
    @SerializedName("how_it_works") var howItWorks: ArrayList<String>?
)
