package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class SetOrderTypePageInfoResponse(
    @SerializedName("static_text")                      var mStaticText: SetOrderTypePageStaticTextResponse?,
    @SerializedName("how_it_works_list")                var mHowItWorkList: ArrayList<HelpScreenItemResponse>?
)

data class SetOrderTypePageStaticTextResponse(
    @SerializedName("dialog_heading_activate_prepaid_orders")   var dialog_heading_activate_prepaid_orders: String?,
    @SerializedName("dialog_heading_note_for_prepaid_orders")   var dialog_heading_note_for_prepaid_orders: String?,
    @SerializedName("dialog_message_note_for_prepaid_orders")   var dialog_message_note_for_prepaid_orders: String?,
    @SerializedName("heading_complete_below_steps")             var heading_complete_below_steps: String?,
    @SerializedName("heading_how_does_prepaid_orders_works")    var heading_how_does_prepaid_orders_works: String?,
    @SerializedName("heading_set_orders_type")                  var heading_set_orders_type: String?,
    @SerializedName("message_change_delivery_charge")           var message_change_delivery_charge: String?,
    @SerializedName("message_complete_kyc")                     var message_complete_kyc: String?,
    @SerializedName("text_activate")                            var text_activate: String?,
    @SerializedName("text_i_accept")                            var text_i_accept: String?,
    @SerializedName("text_kyc_completed")                       var text_kyc_completed: String?,
    @SerializedName("text_complete_kyc")                        var text_complete_kyc: String?,
    @SerializedName("text_change_delivery_charge")              var text_change_delivery_charge: String?,
    @SerializedName("dialog_text_alert")                        var dialog_text_alert: String
)