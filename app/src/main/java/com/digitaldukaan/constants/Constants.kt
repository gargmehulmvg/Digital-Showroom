package com.digitaldukaan.constants

class Constants {
    companion object {
        //TIMER
        const val OTP_SUCCESS_TIMER                             = 500L
        const val LOCATION_REQUEST_CODE                         = 1001
        const val BACK_PRESS_INTERVAL                           = 2_000L
        const val SHINE_ANIMATION_INTERVAL                      = 2_000L
        const val STORE_CREATION_PROGRESS_ANIMATION_INTERVAL    = 5_000L
        const val RESEND_OTP_TIMER                              = 30_000L
        const val AUTO_DISMISS_PROGRESS_DIALOG_TIMER            = 1_000L
        const val TIMER_INTERVAL                                = 500L
        const val ORDER_DELAY_INTERVAL                          = 150L
        const val TOOL_TIP_TIMER_INTERVAL                       = 3_000L
        //REQUEST_CODE
        const val CREDENTIAL_PICKER_REQUEST                     = 1001
        const val IMAGE_PICK_REQUEST_CODE                       = 1002
        const val CONTACT_REQUEST_CODE                          = 1003
        const val CROP_IMAGE_REQUEST_CODE                       = 1004
        const val EXTERNAL_STORAGE_REQUEST_CODE                 = 1005
        const val STORAGE_REQUEST_CODE                          = 1006
        const val EMAIL_REQUEST_CODE                            = 1007
        //ERROR_CODE
        const val ERROR_CODE_UN_AUTHORIZED_ACCESS               = 401
        const val ERROR_CODE_FORBIDDEN_ACCESS                   = 403
        const val ERROR_CODE_FORCE_UPDATE                       = 410
        const val ERROR_MESSAGE_UN_AUTHORIZED_ACCESS            = "unauthorized access"
        const val SHARED_PREF_NAME                              = "DigitalDukaanPrefs"
        const val USER_AUTH_TOKEN                               = "USER_AUTH_TOKEN"
        const val STORE_ID                                      = "STORE_ID"
        const val USER_ID                                       = "USER_ID"
        const val USER_MOBILE_NUMBER                            = "USER_MOBILE_NUMBER"
        const val STORE_NAME                                    = "STORE_NAME"
        const val CLIPBOARD_LABEL                               = "label"
        const val SETTINGS                                      = "settings"
        const val DOTPE_OFFICIAL_URL_CLIPBOARD                  = "https://web.dotpe.in"
        const val DOTPE_OFFICIAL_URL                            = "Go to web.dotpe.in"
        const val WEB_VIEW_URL_EDIT_SOCIAL_MEDIA_POST           = "edit-social-media-template"
        const val TEXT_YES                                      = "yes"
        const val TEXT_NO                                       = "no"
        const val APP_SECRET_KEY                                = "wFqjaY2jR85nMmgI7wqvPHd6mYaEN9a1"
        const val ANALYTICS_PRODUCTION_URL                      = "https://api.dotshowroom.in/"
        //KEYS
        const val KEY_BUY_DOMAIN                                = "buy-domain"
        const val KEY_BUY_THEMES                                = "buy-themes"
        const val KEY_ADD_PRODUCT                               = "add-product"
        const val KEY_DONT_SHOW_MESSAGE_AGAIN                   = "KEY_DONT_SHOW_MESSAGE_AGAIN"
        const val KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK             = "KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK"
        //ACTIONS
        const val ACTION_LOGOUT                                 = "logout"
        const val ACTION_STORE_DESCRIPTION                      = "store_description"
        const val ACTION_BANK_ACCOUNT                           = "bank_account"
        const val ACTION_BUSINESS_TYPE                          = "bussiness_type"
        const val ACTION_EDIT_STORE_LINK                        = "edit_store_link"
        const val ACTION_STORE_NAME                             = "store_name"
        const val ACTION_STORE_LOCATION                         = "store_location"
        const val ACTION_BUSINESS_CREATIVE                      = "business_creatives"
        const val ACTION_SOCIAL_CREATIVE                        = "social_creatives"
        const val ACTION_QR_DOWNLOAD                            = "qr-download"
        const val ACTION_SHARE_STORE                            = "share-store"
        const val ACTION_CATALOG_WHATSAPP                       = "catalog-whatsapp"
        const val ACTION_THEME_DISCOVER                         = "theme-discover"
        const val ACTION_THEME_EXPLORE                          = "theme-explore"
        const val ACTION_KYC_STATUS                             = "kyc_status"
        const val ACTION_ADD_BANK                               = "add-bank"
        const val ACTION_REJECT_ORDER                           = "reject-order"
        const val ACTION_SHARE_BILL                             = "share-bill"
        const val ACTION_SEND_BILL                              = "send-bill"
        const val ACTION_SET_PREPAID_ORDER                      = "set-prepaid-order"
        const val ACTION_PREPAID_ORDER                          = "prepaid-order"
        const val ACTION_DOWNLOAD_BILL                          = "download-bill"
        const val ACTION_CASH_COLLECTED                         = "cash-collected"
        const val ACTION_DOMAIN_SUCCESS                         = "domain-success"
        const val ACTION_KYC                                    = "kyc"
        const val ACTION_DELIVERY_CHARGES                       = "delivery-charges"
        const val ACTION_BOTTOM_SHEET                           = "bottom-sheet"
        const val ACTION_PROMO_CODE                             = "promo-code"
        const val ACTION_EMAIL_AUTHENTICATION                   = "email-authentication"
        const val ACTION_MARK_OUT_FOR_DELIVERY                  = "mark-out-for-delivery"
        const val ACTION_HOW_TO_SHIP                            = "how-to-ship"
        const val ACTION_PRODUCT_DISCOUNT                       = "product-discount"
        const val ACTION_BESTSELLER                             = "bestseller"
        const val ACTION_LOGO                                   = "logo"
        const val ACTION_DESCRIPTION                            = "description"
        const val ACTION_LOCATION                               = "address"
        const val ACTION_BUSINESS                               = "business"
        const val ACTION_BANK                                   = "bank"
        const val ACTION_ADD_PRODUCT                            = "add-products"
        const val ACTION_MY_PROFILE                             = "my-profile"
        const val ACTION_STORE_CONTROLS                         = "store-controls"
        const val ACTION_GST_ADD                                = "gst-add"
        const val ACTION_GST_PENDING                            = "gst-pending"
        const val ACTION_GST_VERIFIED                           = "gst-verified"
        const val ACTION_GST_REJECTED                           = "gst-rejected"
        //PAGES
        const val PAGE_REFER                                    = "refer"
        const val PAGE_HELP                                     = "help"
        const val PAGE_ORDER_NOTIFICATIONS                      = "order-notification"
        const val PAGE_FEEDBACK                                 = "Feedback"
        const val PAGE_APP_SETTINGS                             = "Sitemap"
        const val PAGE_REWARDS                                  = "RewardsPage"
        const val PAGE_MY_PAYMENTS                              = "my-payments"
        //ERROR_CODES
        const val ERROR_DOMAIN_ALREADY_EXISTS                   = "domain_already_exists"
        //GRID_LAYOUT_SPAN_TYPE
        const val SPAN_TYPE_FULL_WIDTH                          = "fullWidth"
        const val SPAN_TYPE_FULL_HEIGHT                         = "fullHeight"
        //ORDER_TYPE
        const val ORDER_TYPE_ADDRESS                            = 1
        const val ORDER_TYPE_PICK_UP                            = 2
        const val ORDER_TYPE_SELF                               = 3
        const val ORDER_TYPE_SELF_IMAGE                         = 4
        const val ORDER_TYPE_POSTPAID                           = 0
        const val ORDER_TYPE_PREPAID                            = 1
        //DISPLAY_STATUS
        const val DS_NEW                                        = "new"                             // When order comes but not opened by merchant
        const val DS_SEND_BILL                                  = "sendBill"                        // When order comes opened the order but didn't send bill and exit
        const val DS_BILL_SENT                                  = "billSent"                        // When order comes and send bill but payment is due
        const val DS_CASH_MARKED                                = "cashMarked"                      // When order comes & marked as cash
        const val DS_PAID_ONLINE                                = "paidOnline"                      // When order comes & order is paid online by customer
        const val DS_REJECTED                                   = "rejected"                        // Order rejected byt customer
        const val DS_COMPLETED_CASH                             = "completedCash"                   // When order comes & marked as cash
        const val DS_COMPLETED_ONLINE                           = "completedOnline"                 // Order self checklist by merchant :: Paid Online
        const val DS_PENDING_PAYMENT_LINK                       = "pendingPaymentLink"              // Order self checklist by merchant :: Paid Online
        const val DS_MARK_READY                                 = "markReady"                       // Merchant seen & start making the order for customer
        const val DS_PREPAID_PICKUP_READY                       = "prepaidPickupReady"
        const val DS_PREPAID_DELIVERY_READY                     = "prepaidDeliveryReady"
        const val DS_PREPAID_PICKUP_COMPLETED                   = "prepaidPickupCompleted"
        const val DS_PREPAID_DELIVERY_COMPLETED                 = "prepaidDeliveryCompleted"
        const val DS_OUT_FOR_DELIVERY                           = "outForDelivery"                  // Order out for delivery
        //MODES
        const val MODE_PENDING                                  = "mode_pending"
        const val MODE_COMPLETED                                = "mode_completed"
        const val MODE_PREPAID                                  = "mode_prepaid"
        const val MODE_POSTPAID                                 = "mode_postpaid"
        const val MODE_SETTLEMENTS                              = "settlements"
        const val MODE_ORDERS                                   = "orders"
        const val MODE_SETTLED                                  = "Settled"
        const val MODE_SMS                                      = "sms"
        const val MODE_WHATS_APP                                = "whatsapp"
        const val MODE_COUPON_TYPE_PERCENTAGE                   = "percentage"
        const val MODE_COUPON_TYPE_FLAT                         = "flat"
        const val MODE_ACTIVE                                   = "active"
        const val MODE_INACTIVE                                 = "inactive"
        const val MODE_PROMO_CODE_ACTIVE                        = "A"
        const val MODE_PROMO_CODE_DE_ACTIVE                     = "D"
        const val MODE_SHARE_STORE                              = 1
        const val MODE_SHARE_TEMPLATE                           = 2
        const val MODE_SHARE_PRODUCTS                           = 3
        const val MODE_SHARE_QR                                 = 4
        const val MODE_CREATE_BILL                              = 5
        const val MODE_SEND_BILL                                = 6
        const val MODE_SHARE_BILL                               = 7
        const val MODE_OUT_FOR_DELIVERY                         = 8
        const val MODE_READY_FOR_PICKUP                         = 9
        const val MODE_GET_MY_CATALOGUE                         = 10
        const val MODE_GOOGLE_ADS                               = 11
        const val MODE_ORDER_SHIPPED                            = 12
        //ORDER_STATUS
        const val StatusCustomerUpdated                         = 1
        const val StatusMerchantUpdated                         = 2
        const val StatusRejected                                = 3   //(Status_message : Merchant/Customer Rejected)
        const val StatusActive                                  = 4   //(Accepted Tab in merchant)
        const val StatusReady                                   = 5   //(For delivery / pickup)
        const val StatusOutForDelivery                          = 6
        const val StatusCompleted                               = 7
        const val StatusCancelled                               = 8
        const val StatusSeenByMerchant                          = 9
        //BASE64 APIs END POINT
        const val BASE64_STORE_LOGO                             = "store_logo"
        const val BASE64_STORE_ITEMS                            = "store_items"
        const val BASE64_ORDER_BILL                             = "order_bill"
        const val BASE64_THEMES                                 = "themes"
        //CREATOR_TYPE
        const val CREATOR_TYPE_MERCHANT                         = 1
        const val CREATOR_TYPE_CUSTOMER                         = 2
        //NEW RELEASE TYPES
        const val NEW_RELEASE_TYPE_WEBVIEW                      = "webview"
        const val NEW_RELEASE_TYPE_PREMIUM                      = "premium"
        const val NEW_RELEASE_TYPE_NEW                          = "new"
        const val NEW_RELEASE_TYPE_CUSTOM_DOMAIN                = "custom-domain"
        const val NEW_RELEASE_TYPE_PREPAID_ORDER                = "prepaid-order"
        const val NEW_RELEASE_TYPE_PAYMENT_MODES                = "payment-mode"
        const val NEW_RELEASE_TYPE_TRENDING                     = "trending"
        const val NEW_RELEASE_TYPE_EXTERNAL                     = "external"
        const val NEW_RELEASE_TYPE_GOOGLE_ADS                   = "google-ads"
        //EDIT PHOTO MODES
        const val EDIT_PHOTO_MODE_MOBILE                        = "mobile"
        const val EDIT_PHOTO_MODE_DESKTOP                       = "desktop"
        //MODES
        const val MODE_CROP                                     = "crop"
        const val MODE_ADD_PRODUCT                              = "add_product"
        const val MODE_PRODUCT_LIST                             = "product_list"
        //MODE ORDER STATUS
        const val ORDER_STATUS_SUCCESS                          = "Success"
        const val ORDER_STATUS_REJECTED                         = "Rejected"
        const val ORDER_STATUS_IN_PROGRESS                      = "In-Progress"
        const val ORDER_STATUS_REFUND_SUCCESS                   = "refund-success"
        const val ORDER_STATUS_PAYOUT_SUCCESS                   = "payout-success"
        const val ORDER_STATUS_LOCKED                           = "Locked"
        //MODE DELIVERY CHARGE
        const val UNKNOWN_DELIVERY_CHARGE                       = 0
        const val FREE_DELIVERY                                 = 1
        const val FIXED_DELIVERY_CHARGE                         = 2
        const val CUSTOM_DELIVERY_CHARGE                        = 3
        //ITEM_TYPE_CHARGE
        const val ITEM_TYPE_DELIVERY_CHARGE                     = "delivery_charge"
        const val ITEM_TYPE_CHARGE                              = "charge"
        const val ITEM_TYPE_LIST                                = "list"
        const val ITEM_TYPE_CATALOG                             = "catalog"
        const val ITEM_TYPE_DISCOUNT                            = "discount"
        //CTA_TYPES
        const val CTA_TYPE_BORDER: String                       = "border"
        const val CTA_TYPE_SOLID: String                        = "solid"
        //MODE DELIVERY CHARGE
        const val MEDIA_TYPE_IMAGES                             = 1
        const val MEDIA_TYPE_VIDEOS                             = 2
        //MODE BOTTOM NAV BAR
        const val NAV_BAR_ORDERS                                = 1
        const val NAV_BAR_CATALOG                               = 2
        const val NAV_BAR_PREMIUM                               = 3
        const val NAV_BAR_MARKETING                             = 4
        const val NAV_BAR_SETTINGS                              = 5
        //NAV_PAGE
        const val PAGE_ORDER                                    = "page_order"
        const val PAGE_CATALOG                                  = "page_catalog"
        const val PAGE_PREMIUM                                  = "page_premium"
        const val PAGE_MARKETING                                = "page_marketing"
        const val PAGE_SETTINGS                                 = "page_settings"
        const val STORE_LOGO                                    = "store_logo"
        const val STORE_CONTROLS                                = "store_controls"
        const val STORE_PROFILE                                 = "store_profile"
        //BILL_TYPE
        const val BILL_TYPE_PDF                                 = "pdf"
        //STAFF_INVITATION_CODE
        const val STAFF_INVITATION_CODE_EXIT                    = 0
        const val STAFF_INVITATION_CODE_ACCEPT                  = 1
        const val STAFF_INVITATION_CODE_REJECT                  = 2
    }
}