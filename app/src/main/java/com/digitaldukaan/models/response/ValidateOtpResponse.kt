package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ValidateOtpResponse (
    @SerializedName("status")                   var mIsSuccessStatus: Boolean,
    @SerializedName("new_user")                 var mIsNewUser: Boolean,
    @SerializedName("is_invitation_available")  var mIsInvitationShown: Boolean,
    @SerializedName("message")                  var mMessage: String?,
    @SerializedName("user_id")                  var mUserId: String?,
    @SerializedName("phone")                    var mUserPhoneNumber: String?,
    @SerializedName("auth_token")               var mUserAuthToken: String?,
    @SerializedName("store")                    var mStore: StoreResponse?,
    @SerializedName("staff_invitation")         var mStaffInvitation: StaffInvitationResponse?,
    @SerializedName("permissions_map"   )       var mPermissionMap: HashMap<String, Boolean>?,
    @SerializedName("auto_verify_time")         var mAutoVerifyTime: Int?
)

data class ValidateUserResponse (
    @SerializedName("user")                     var user: UserResponse,
    @SerializedName("store")                    var store: StoreResponse?,
    @SerializedName("is_invitation_available")  var mIsInvitationShown: Boolean,
    @SerializedName("staff_invitation")         var mStaffInvitation: StaffInvitationResponse?
)

data class UserResponse (
    @SerializedName("user_id")              var userId: String,
    @SerializedName("phone")                var phone: String?,
    @SerializedName("new_user")             var isNewUser: Boolean,
    @SerializedName("auth_token")           var authToken: String?
)

data class UserStoreInfoResponse (
    @SerializedName("status")               var mIsSuccessStatus: Boolean?,
    @SerializedName("name")                 var name: String?,
    @SerializedName("domain")               var domain: String?,
    @SerializedName("store_url")            var storeUrl: String?,
    @SerializedName("store_type")           var storeType: String?,
    @SerializedName("logo_image")           var logoImage: String?,
    @SerializedName("reference_store_id")   var referenceStoreId: Int?,
    @SerializedName("description")          var description: String?
)

data class StoreOwnerResponse (
    @SerializedName("user_id")              var userId: String,
    @SerializedName("verify_phone")         var verifyPhone: String?,
    @SerializedName("phone")                var phone: String?,
    @SerializedName("language_id")          var languageId: String?,
    @SerializedName("permissions")          var permissionArray: ArrayList<Int>
)

data class StoreBusinessResponse (
    @SerializedName("business_id")          var businessId: String,
    @SerializedName("business_name")        var businessName: String?,
    @SerializedName("image")                var image: String?
)

data class ValidateOtpErrorResponse (
    @SerializedName("status")               var mIsSuccessStatus: Boolean,
    @SerializedName("errorType")            var mErrorType: String?,
    @SerializedName("message")              var mMessage: String?
)