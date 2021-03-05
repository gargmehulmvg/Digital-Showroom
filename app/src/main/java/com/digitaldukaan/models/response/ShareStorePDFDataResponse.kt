package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ShareStorePDFDataResponse(
    @SerializedName("status") var mStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("keys") var mShareStorePDFDataItem: ShareStorePDFDataItemResponse?
)

data class ShareStorePDFDataItemResponse(
    @SerializedName("heading") var heading: String,
    @SerializedName("sub_heading") var subHeading: String?,
    @SerializedName("creating_pdf") var creatingPdf: String?,
    @SerializedName("whats_app_text") var whatsAppText: String?,
    @SerializedName("how_it_works") var howItWorks: ArrayList<String>?
)
