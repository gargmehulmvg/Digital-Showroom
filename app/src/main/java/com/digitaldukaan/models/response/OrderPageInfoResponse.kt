package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class OrderPageInfoResponse(
    @SerializedName("zero_order")                       var mIsZeroOrder: ZeroOrderPageResponse,
    @SerializedName("is_take_order_on")                 var mIsTakeOrder: Boolean,
    @SerializedName("take_order_image")                 var mTakeOrderImage: String,
    @SerializedName("is_search_on")                     var mIsSearchOrder: Boolean,
    @SerializedName("help_page")                        var mIsHelpOrder: HelpPageResponse,
    @SerializedName("banners")                          var mBannerList: ArrayList<HomePageBannerResponse>,
    @SerializedName("options_menu_list")                 var optionMenuList: ArrayList<TrendingListResponse>,
    @SerializedName("is_analytics")                     var mIsAnalyticsOrder: Boolean,
    @SerializedName("completed_order_count")            var mCompletedOrderCount: Int,
    @SerializedName("pending_order_count")              var mPendingOrderCount: Int,
    @SerializedName("store")                            var mStoreInfo: StoreResponse?,
    @SerializedName("static_text")                      var mOrderPageStaticText: OrderPageStaticTextResponse?
)

data class ZeroOrderPageResponse(
    @SerializedName("is_active")                        var mIsActive: Boolean,
    @SerializedName("deep_link")                        var mUrl: String
)

data class HelpPageResponse(
    @SerializedName("is_active")                        var mIsActive: Boolean,
    @SerializedName("deep_link")                        var mUrl: String
)

data class HomePageBannerResponse(
    @SerializedName("banner_url")                       var mBannerUrl: String,
    @SerializedName("action")                           var mAction: String,
    @SerializedName("deep_link")                        var mDeepLinkUrl: String
)

data class OrderPageStaticTextResponse(
    @SerializedName("new")                              var newText: String?,
    @SerializedName("text_send_bill")                   var sendBillText: String?,
    @SerializedName("text_sent_bill")                   var sentBillText: String?,
    @SerializedName("text_payment_pending")             var paymentPending: String?,
    @SerializedName("text_self_billed")                 var selfBilled: String?,
    @SerializedName("text_pickup_order")                var pickUpOrder: String?,
    @SerializedName("heading_search_dialog")            var heading_search_dialog: String,
    @SerializedName("search_dialog_selection_one")      var search_dialog_selection_one: String,
    @SerializedName("search_dialog_selection_two")      var search_dialog_selection_two: String,
    @SerializedName("search_dialog_button_text")        var search_dialog_button_text: String,
    @SerializedName("text_phone_number")                var text_order_id: String,
    @SerializedName("msg_double_click_to_exit")         var msg_double_click_to_exit: String,
    @SerializedName("fetching_orders")                  var fetching_orders: String,
    @SerializedName("heading_order_page")               var heading_order_page: String,
    @SerializedName("text_pending")                     var text_pending: String,
    @SerializedName("text_completed")                   var text_completed: String,
    @SerializedName("text_rejected")                    var text_rejected: String,
    @SerializedName("text_paid_online")                 var text_paid_online: String,
    @SerializedName("text_today")                       var text_today: String,
    @SerializedName("error_mandatory_field")            var error_mandatory_field: String,
    @SerializedName("text_add_new_order")               var text_add_new_order: String,
    @SerializedName("bottom_sheet_click_bill_photo")    var bottom_sheet_click_bill_photo: String,
    @SerializedName("bottom_sheet_take_order_message")  var bottom_sheet_take_order_message: String,
    @SerializedName("bottom_sheet_create_a_new_bill")   var bottom_sheet_create_a_new_bill: String,
    @SerializedName("dialog_message")                   var dialog_message: String,
    @SerializedName("dialog_check_box_text")            var dialog_check_box_text: String,
    @SerializedName("dialog_text_no")                   var dialog_text_no: String,
    @SerializedName("dialog_text_yes")                  var dialog_text_yes: String,
    @SerializedName("dialog_text_alert")                var dialog_text_alert: String,
    @SerializedName("text_mark_ready")                  var markReadyText: String?,
    @SerializedName("text_out_for_delivery")            var outForDeliveryText: String?,
    @SerializedName("text_ready_for_pickup")            var readyForPickupText: String?,
    @SerializedName("text_mark_out_for_delivery")       var markOutForDeliveryText: String?,
    @SerializedName("text_delivery_time_is_set_as")     var deliveryTimeIsSetAsText: String?,
    @SerializedName("text_pickup_order_success")        var pickUpOrderSuccess: String?,
    @SerializedName("dialog_message_prepaid_delivery")  var dialog_message_prepaid_delivery: String,
    @SerializedName("dialog_message_prepaid_pickup")    var dialog_message_prepaid_pickup: String,

    @SerializedName("bottom_sheet_heading_enter_contact_number")    var bottom_sheet_heading_enter_contact_number: String,
    @SerializedName("bottom_sheet_hint_enter_contact_number")       var bottom_sheet_hint_enter_contact_number: String,
    @SerializedName("text_send_link")                   var text_send_link: String,
    @SerializedName("text_whatsapp")                    var text_whatsapp: String,
    @SerializedName("bottom_sheet_message_customer_pay")var bottom_sheet_message_customer_pay: String,
    @SerializedName("bottom_sheet_heading_send_link")   var bottom_sheet_heading_send_link: String,
    @SerializedName("text_sms")                         var text_sms: String,
    @SerializedName("heading_share_payment_link")       var heading_share_payment_link: String,
    @SerializedName("bottom_sheet_message_payment_link")var bottom_sheet_message_payment_link: String,
    @SerializedName("text_send_payment_link")           var text_send_payment_link: String,
    @SerializedName("payment_link")                     var payment_link: String,
    @SerializedName("text_payment_link")                var text_payment_link: String
)