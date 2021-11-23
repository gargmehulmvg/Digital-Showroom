package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StaffMemberDetailsResponse(
        @SerializedName("is_invitation_available")  var mIsInvitationAvailable: Boolean,
        @SerializedName("staff_invitation")         var mStaffInvitation: StaffInvitationResponse,
        @SerializedName("store_id")                 var storeId: String?,
        @SerializedName("user_phone")               var userPhone: String?,
        @SerializedName("name")                     var name: String?,
        @SerializedName("role")                     var role: String?,
        @SerializedName("permissions")              var permissions: ArrayList<Int>?,
        @SerializedName("status")                   var status: String?,
        @SerializedName("count")                    var count: String?,
        @SerializedName("permissions_map")          var permissionsMap: HashMap<String,Boolean>?
)
