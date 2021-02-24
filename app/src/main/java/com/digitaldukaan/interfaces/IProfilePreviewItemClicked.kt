package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse

interface IProfilePreviewItemClicked {

    fun onProfilePreviewItemClicked(profilePreviewResponse: ProfilePreviewSettingsKeyResponse)
}