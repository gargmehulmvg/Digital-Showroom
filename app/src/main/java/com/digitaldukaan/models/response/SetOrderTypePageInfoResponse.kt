package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class SetOrderTypePageInfoResponse(
    @SerializedName("static_text")                              var mStaticText: SetOrderTypePageStaticTextResponse?,
    @SerializedName("post_paid")                                var mPostPaidResponse: PostpaidResponse?,
    @SerializedName("pre_paid")                                 var mPrePaidResponse: PostpaidResponse?,
    @SerializedName("both_paid")                                var mBothPaidResponse: PostpaidResponse?,
    @SerializedName("payment_method")                           var mPaymentMethod: Int,
    @SerializedName("is_subscription_purchased")                var mIsSubscriptionPurchased: Boolean,
    @SerializedName("how_it_works_list")                        var mHowItWorkList: ArrayList<HelpScreenItemResponse>?,
    @SerializedName("unlock_options_list")                      var mUnlockOptionList: ArrayList<UnlockOptionItemList>?
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

data class UnlockOptionItemList(
    @SerializedName("heading")                                  var heading: String?,
    @SerializedName("sub_heading")                              var subHeading: String?,
    @SerializedName("is_editable")                              var isEditable: Boolean,
    @SerializedName("action")                                   var action: String?
)

data class PostpaidResponse(
    @SerializedName("id")                                       var id: Int,
    @SerializedName("text")                                     var text: String?,
    @SerializedName("is_completed")                             var isCompleted: Boolean,
    @SerializedName("text_and_status")                          var setOrderTypeItemList: ArrayList<SetOrderTypeItemResponse>?
)

data class SetOrderTypeItemResponse(
    @SerializedName("text")                                     var text: String?,
    @SerializedName("status")                                   var status: String?
)