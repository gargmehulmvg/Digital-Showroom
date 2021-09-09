package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class StoreUserMailDetailsRequest(
    @SerializedName("first_name")   var firstName: String?,
    @SerializedName("last_name")    var lastName: String?,
    @SerializedName("sign_in_id")   var signInId: String?,
    @SerializedName("photo")        var photo: String?,
    @SerializedName("email_id")     var emailId: String?
)