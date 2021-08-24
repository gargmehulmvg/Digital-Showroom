package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StoreUserPageInfoResponse(
    @SerializedName("gmail_cdn")                var gmailCdn: String,
    @SerializedName("gmail_guide_line_list")    var gmailGuideLineList: ArrayList<String>?,
    @SerializedName("user_details")             var gmailUserDetails: ArrayList<GmailUserDetailResponse>?,
    @SerializedName("static_text")              var staticText: StoreUserPageInfoStaticTextResponse?
)

data class GmailUserDetailResponse(
    @SerializedName("first_name")   var first_name: String,
    @SerializedName("last_name")    var last_name: String,
    @SerializedName("email_id")     var email_id: String,
    @SerializedName("dob")          var dob: String,
    @SerializedName("mode")         var mode: String,
    @SerializedName("phone")        var phone: String
)

data class StoreUserPageInfoStaticTextResponse(
    @SerializedName("button_sign_in_with_another_account")  var button_sign_in_with_another_account: String,
    @SerializedName("header_add_gmail_account")             var header_add_gmail_account: String,
    @SerializedName("header_linked_gmail_account")          var header_linked_gmail_account: String,
    @SerializedName("heading_email_text")                   var heading_email_text: String,
    @SerializedName("button_sign_in_with_google")           var button_sign_in_with_google: String
)