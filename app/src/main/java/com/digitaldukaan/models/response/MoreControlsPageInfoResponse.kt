package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class MoreControlsPageInfoResponse(
    @SerializedName("store_control_items")      var storeControlItemsList: ArrayList<MoreControlsItemResponse>,
    @SerializedName("store")                    var store: StoreResponse,
    @SerializedName("static_text")              var staticText: MoreControlsStaticTextResponse
)

data class MoreControlsItemResponse(
    @SerializedName("heading")                  var heading: String,
    @SerializedName("children")                 var children: ArrayList<MoreControlsInnerItemResponse>
)

data class MoreControlsInnerItemResponse(
    @SerializedName("heading")                  var heading: String,
    @SerializedName("sub_heading")              var subHeading: String,
    @SerializedName("value")                    var value: String,
    @SerializedName("is_expandable")            var isExpandable: Boolean,
    @SerializedName("is_clickable")             var isClickable: Boolean,
    @SerializedName("is_locked")                var isLocked: Boolean,
    @SerializedName("is_new")                   var isNew: Boolean,
    @SerializedName("text_unlock_now")          var textUnlockNow: String,
    @SerializedName("action")                   var action: String
)

data class MoreControlsStaticTextResponse(
    @SerializedName("heading_page")             var heading_page: String?,
    @SerializedName("heading_tap_the_icon")     var heading_tap_the_icon: String?,
    @SerializedName("text_closed")              var text_closed: String?,
    @SerializedName("text_delivery")            var text_delivery: String?,
    @SerializedName("text_new")                 var text_new: String?,
    @SerializedName("text_off")                 var text_off: String?,
    @SerializedName("text_on")                  var text_on: String?,
    @SerializedName("text_open")                var text_open: String?,
    @SerializedName("text_pickup")              var text_pickup: String?,
    @SerializedName("text_ruppee_symbol")       var text_ruppee_symbol: String?,
    @SerializedName("text_store")               var text_store: String?,
    @SerializedName("text_unlock_now")          var text_unlock_now: String?
)