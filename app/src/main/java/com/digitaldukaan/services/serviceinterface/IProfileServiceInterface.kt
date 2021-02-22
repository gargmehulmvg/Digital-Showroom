package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.ProfileResponse

interface IProfileServiceInterface {

    fun onProfileResponse(profileResponse: ProfileResponse)

    fun onProfileDataException(e: Exception)
}