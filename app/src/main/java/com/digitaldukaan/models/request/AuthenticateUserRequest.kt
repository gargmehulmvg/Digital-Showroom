package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class AuthenticateUserRequest(
    @SerializedName("auth_token") var mAuthToken: String
)