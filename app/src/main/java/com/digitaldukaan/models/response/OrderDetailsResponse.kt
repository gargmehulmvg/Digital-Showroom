package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class OrderDetailsResponse (
    @SerializedName("store_id")                     var storeId: Int?,
    @SerializedName("order_id")                     var orderId: Int?,
    @SerializedName("merchant_id")                  var merchantId: Int?,
    @SerializedName("payment_status")               var paymentStatus: Int?,
    @SerializedName("status")                       var status: Int?,
    @SerializedName("order_type")                   var orderType: Int?,
    @SerializedName("amount")                       var amount: Double?,
    @SerializedName("pay_amount")                   var payAmount: Double?,
    @SerializedName("discount")                     var discount: Double?,
    @SerializedName("promo_discount")               var promoDiscount: Double?,
    @SerializedName("delivery_charge")              var deliveryCharge: Double?,
    @SerializedName("extra_charges")                var extraCharges: Double?,
    @SerializedName("extra_charge_name")            var extraChargesName: String?,
    @SerializedName("instruction")                  var instruction: String?,
    @SerializedName("phone")                        var phone: String?,
    @SerializedName("display_status")               var displayStatus: String?,
    @SerializedName("status_message")               var statusMessage: String?,
    @SerializedName("order_hash")                   var orderHash: String?,
    @SerializedName("transaction_id")               var transactionId: String,
    @SerializedName("image_link")                   var imageLink: String?,
    @SerializedName("created_at")                   var createdAt: String?,
    @SerializedName("updated_at")                   var updatedAt: String?,
    @SerializedName("prepaid_flag")                 var prepaidFlag: Int,
    @SerializedName("order_payment_status")         var orderPaymentStatus: OrderPaymentStatusResponse?,
    @SerializedName("digital_receipt")              var digitalReceipt: String?,
    @SerializedName("digital_receipt_extension")    var digitalReceiptExtension: String?,
    @SerializedName("items")                        var orderDetailsItemsList: ArrayList<OrderDetailItemResponse>?,
    @SerializedName("delivery_info")                var deliveryInfo: DeliveryInfoItemResponse?
)

data class OrderDetailItemResponse(
    @SerializedName("order_item_id")            var orderItemId: Int,
    @SerializedName("item_id")                  var itemId: String?,
    @SerializedName("item_name")                var itemName: String?,
    @SerializedName("quantity")                 var quantity: Int,
    @SerializedName("item_quantity")            var itemQuantity: String?,
    @SerializedName("item_price")               var itemPrice: Double?,
    @SerializedName("amount")                   var amount: Double,
    @SerializedName("actual_amount")            var actualAmount: Double,
    @SerializedName("discounted_price")         var discountedPrice: Double,
    @SerializedName("item_status")              var itemStatus: Int,
    @SerializedName("item_type")                var itemType: String?,
    @SerializedName("creator_type")             var creatorType: Int?,
    @SerializedName("variant_id")               var variantId: Int,
    @SerializedName("variant_name")             var variantName: String?,
    @SerializedName("thumbnail_url")            var imageUrl: String?,
    var isItemEditable: Boolean
)

data class OrderPaymentStatusResponse(
    @SerializedName("key")                      var key: String?,
    @SerializedName("value")                    var value: String?,
    @SerializedName("cta")                      var cta: Any?
)

data class OrderDetailMainResponse(
    @SerializedName("order")                    var orders: OrderDetailsResponse?,
    @SerializedName("static_text")              var staticText: OrderDetailsStaticTextResponse?,
    @SerializedName("options_menu")             var optionMenuList: ArrayList<TrendingListResponse>,
    @SerializedName("delivery_details")         var deliveryDetails: OrderDetailDeliveryResponse?,
    @SerializedName("prepaid_delivery_details") var prepaidDeliveryDetails: OrderDetailPrepaidDeliveryResponse?,
    @SerializedName("delivery_partner_details") var deliveryPartnerDetails: DeliveryPartnerCopyTextResponse?,
    @SerializedName("promo_code_details")       var promoCodeDetails: OrderDetailPromoCodeResponse?,
    @SerializedName("send_bill_action")         var sendBillAction: String,
    @SerializedName("footer_layout")            var footerLayout: String,
    @SerializedName("is_header_layout_visible") var isHeaderLayoutVisible: Boolean,
    @SerializedName("store_services")           var storeServices: StoreServicesResponse?
)

data class DeliveryPartnerCopyTextResponse(
    @SerializedName("is_enabled")               var isEnabled: Boolean,
    @SerializedName("delivery_display_status")  var deliveryDisplayStatus: String?,
    @SerializedName("inner_bg_color")           val innerBgColor: String? = null,
    @SerializedName("outer_bg_color")           val outerBgColor: String? = null,
    @SerializedName("cta")                      var ctaResponse: CommonCtaResponse?
)

data class OrderDetailPromoCodeResponse(
    @SerializedName("coupon")                   var coupon: PromoCodeListItemResponse?,
    @SerializedName("percentage_cdn")           var percentageCdn: String?,
    @SerializedName("background_cdn")           var backgroundCdn: String?
)

data class OrderDetailsStaticTextResponse(
    @SerializedName("heading_add_delivery_and_other_charges")   val heading_add_delivery_and_other_charges: String?,
    @SerializedName("heading_customer_details")                 val heading_customer_details: String?,
    @SerializedName("heading_free")                             val heading_free: String?,
    @SerializedName("heading_instructions")                     val heading_instructions: String?,
    @SerializedName("heading_send_bill_to_your_customer")       val heading_send_bill_to_your_customer: String?,
    @SerializedName("text_customer_can_pay_via")                val text_customer_can_pay_via: String?,
    @SerializedName("text_rupees_symbol")                       val text_rupees_symbol: String?,
    @SerializedName("text_refund_successful")                   val text_refund_successful: String?,
    @SerializedName("text_status")                              val text_status: String?,
    @SerializedName("text_quantity")                            val text_quantity: String?,
    @SerializedName("text_packet")                              val text_packet: String?,
    @SerializedName("text_order_id")                            val text_order_id: String?,
    @SerializedName("text_order")                               val text_order: String?,
    @SerializedName("text_new_order")                           val text_new_order: String?,
    @SerializedName("text_name")                                val text_name: String?,
    @SerializedName("text_id")                                  val text_id: String?,
    @SerializedName("hint_other_charges")                       val hint_other_charges: String?,
    @SerializedName("hint_discount")                            val hint_discount: String?,
    @SerializedName("text_add_bank_account")                    val text_add_bank_account: String?,
    @SerializedName("text_address")                             val text_address: String?,
    @SerializedName("text_bank_account_not_found")              val text_bank_account_not_found: String?,
    @SerializedName("text_bill_amount")                         val text_bill_amount: String?,
    @SerializedName("text_city_and_pincode")                    val text_city_and_pincode: String?,
    @SerializedName("text_delivery_charges")                    val text_delivery_charges: String?,
    @SerializedName("text_details")                             val text_details: String?,
    @SerializedName("text_estimate_delivery")                   val text_estimate_delivery: String?,
    @SerializedName("text_landmark")                            val text_landmark: String?,
    @SerializedName("message_customer_paid")                    val message_customer_paid: String?,
    @SerializedName("message_bill_sent_customer_not_paid")      val message_bill_sent_customer_not_paid: String?,
    @SerializedName("text_processing_refund")                   val text_processing_refund: String?,
    @SerializedName("text_send_bill")                           val text_send_bill: String?,
    @SerializedName("error_no_bill_available_to_download")      val error_no_bill_available_to_download: String?,
    @SerializedName("bottom_sheet_reject_order_heading")        var bottom_sheet_reject_order_heading: String,
    @SerializedName("text_reject_order")                        var text_reject_order: String,
    @SerializedName("text_delivery_guy_not_available")          var text_delivery_guy_not_available: String,
    @SerializedName("text_items_are_not_available")             var text_items_are_not_available: String,
    @SerializedName("text_customer_request_cancellation")       var text_customer_request_cancellation: String,
    @SerializedName("dialog_text_alert")                        var dialog_text_alert: String,
    @SerializedName("text_mark_ready")                          var markReadyText: String?,
    @SerializedName("text_out_for_delivery")                    var outForDeliveryText: String?,
    @SerializedName("text_ready_for_pickup")                    var readyForPickupText: String?,
    @SerializedName("text_mark_out_for_delivery")               var markOutForDeliveryText: String?,
    @SerializedName("text_delivery_time_is_set_as")             var deliveryTimeIsSetAsText: String?,
    @SerializedName("text_pickup_order_success")                var pickUpOrderSuccess: String?,
    @SerializedName("dialog_message_prepaid_delivery")          var dialog_message_prepaid_delivery: String,
    @SerializedName("dialog_message_prepaid_pickup")            var dialog_message_prepaid_pickup: String,
    @SerializedName("message_cod")                              var message_cod: String,
    @SerializedName("text_share_bill")                          var text_share_bill: String,
    @SerializedName("text_next")                                var text_next: String,
    @SerializedName("text_delivery_person")                     var text_delivery_person: String,
    @SerializedName("bottom_sheet_heading_how_will_you_ship")   var bottom_sheet_heading_how_will_you_ship: String,
    @SerializedName("bottom_sheet_message2_i_will_ship")        var bottom_sheet_message2_i_will_ship: String,
    @SerializedName("bottom_sheet_sub_message1_select_this")    var bottom_sheet_sub_message1_select_this: String,
    @SerializedName("bottom_sheet_sub_message2_select_this")    var bottom_sheet_sub_message2_select_this: String,
    @SerializedName("bottom_sheet_message1_ship_using_partners")var bottom_sheet_message1_ship_using_partners: String,
    @SerializedName("text_okay")                                var text_okay: String,
    @SerializedName("text_email_id")                            var text_email_id: String,
    @SerializedName("text_alternate_phone")                     var text_alternate_phone: String
)