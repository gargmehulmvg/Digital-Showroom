package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ImagesSearchResponse(
    @SerializedName("status") var mStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("images") var mImagesList: ArrayList<String>?,
    @SerializedName("error_string") var mErrorString: String?
)