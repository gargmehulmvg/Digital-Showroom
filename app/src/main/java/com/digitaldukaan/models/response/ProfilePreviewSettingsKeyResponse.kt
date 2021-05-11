package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProfilePreviewSettingsKeyResponse(
    @SerializedName("heading_text") var mHeadingText: String?,
    @SerializedName("value") var mValue: String?,
    @SerializedName("default_text") var mDefaultText: String?,
    @SerializedName("is_editable") var mIsEditable: Boolean,
    @SerializedName("action") var mAction: String?,
    @SerializedName("design") var mDesign: String?,
    @SerializedName("lock_text") var mLockText: String?
)