package com.digitaldukaan.constants

interface AFInAppEventType {

    companion object {
        const val EVENT_SET_MIN_ORDER_VALUE                         =   "Set_minimum_order_value"
        const val EVENT_SET_MIN_ORDER_VALUE_SAVE                    =   "minimum_order_value_saved"
        const val EVENT_SET_DELIVERY_CHARGE                         =   "Set_delivery_charge"
        const val EVENT_MARKET_VIEW_NOW                             =   "Mkt_View_Now"
        const val EVENT_SKIP_BANK_ACCOUNT                           =   "SkipBankAccount"
        const val EVENT_BANK_ACCOUNT_ADDED                          =   "BankAccountAdded"
        const val EVENT_ORDER_ANALYTICS                             =   "Orders_Analytics"
        const val EVENT_SEARCH_INTENT                               =   "Orders_Search_Intent"
        const val EVENT_SEARCH_CLICK                                =   "Orders_Search"
        const val EVENT_ORDER_REJECTED                              =   "Order Rejected"
        const val EVENT_DOWNLOAD_BILL                               =   "Download Bill"
        const val EVENT_SHARE_BILL                                  =   "Share Bill"
        const val EVENT_BILL_SENT                                   =   "BillSent"
        const val EVENT_SAVE_ITEM                                   =   "SaveItem"
        const val EVENT_MARKET_OFFERS_EDIT                          =   "Mkt_Offers_Edit"
        const val EVENT_MARKET_OFFERS_EDIT_TEXT                     =   "Mkt_Offers_Edit_Text"
        const val EVENT_MARKET_OFFERS_EDIT_BACKGROUND               =   "Mkt_Offers_Edit_Background"
        const val EVENT_MARKET_OFFERS_SHARE                         =   "Mkt_Offers_Share"
        const val EVENT_START_NOW                                   =   "StartNow"
        const val EVENT_KYC_COMPLETE_NOW                            =   "KYC_Complete_Now"
        const val EVENT_GET_OTP                                     =   "GetOTP"
        const val EVENT_OTP_VERIFIED                                =   "OTPVerified"
        const val EVENT_EDIT_THEME                                  =   "Edit_Theme"
        const val EVENT_EDIT_COLOR                                  =   "Edit_Color"
        const val EVENT_SAVE_THEME_COLOR                            =   "Saved_ThemeColor"
        const val EVENT_ENTER_NAME                                  =   "EnterName"
        const val EVENT_ENTER_NAME_SPACE                            =   "Enter Name"
        const val EVENT_EDIT_THEME_IMAGE                            =   "Edit_ThemeImage"
        const val EVENT_MOBILE_BANNER_CROPPED                       =   "Mobile_Banner_Cropped"
        const val EVENT_DESKTOP_BANNER_CROPPED                      =   "Desktop_Banner_Cropped"
        const val EVENT_SAVE_THEME_IMAGE                            =   "Save_Theme_Image"
        const val EVENT_VERIFY_LOCATION_SET                         =   "LocationSet"
        const val EVENT_VERIFY_ORDER_SEEN                           =   "OrderSeen"
        const val EVENT_GENERATE_SELF_BILL                          =   "Generate Self Bill"
        const val EVENT_TAKE_ORDER                                  =   "TakeOrder"
        const val EVENT_VIEW_AS_CUSTOMER                            =   "View_As_Customer"
        const val EVENT_GET_PDF_CATALOG                             =   "Get PDF Catalog"
        const val EVENT_QR_DOWNLOAD                                 =   "Qr Download"
        const val EVENT_ADD_ITEM                                    =   "AddItem"
        const val EVENT_EDIT_STORE_LINK                             =   "Edit_Store_Link"
        const val EVENT_RE_ARRANGE_PAGE_OPEN                        =   "Rearrange section open"
        const val EVENT_STORE_SHARE                                 =   "StoreShare"
        const val EVENT_SAVE_ADDRESS_CHANGES                        =   "Save Address Changes"
        const val EVENT_CATALOG_BUILDER_TRY_NOW                     =   "CM_Try_Now"
        const val EVENT_CATALOG_BUILDER_EXPLORE                     =   "CM_Explore_Categories"
        const val EVENT_MARKET_OFFERS_CATEGORY                      =   "Mkt_Offers_Category"              //best seller :: Product Discount
        const val EVENT_CATALOG_BUILDER_CATEGORY_SELECT             =   "CM_Category_Select"
        const val EVENT_CATALOG_BUILDER_PRODUCT_SELECT              =   "CM_Product_Select"
        const val EVENT_EDIT_STORE_LINK_WARNING                     =   "Edit_Store_Link_Warning"
        const val EVENT_EDIT_STORE_LINK_SAVE                        =   "Edit_Store_Link_Save"
        const val EVENT_UPDATE_STORE_NAME                           =   "Update Store Name"
        const val EVENT_BUSINESS_TYPE_SELECT                        =   "Business Type Select"
        const val EVENT_DELIVERY_MODEL_SELECT                       =   "Delivery_model_select"
        const val EVENT_BULK_UPLOAD_ITEMS                           =   "Bulk_Upload_Items"
        const val EVENT_VIEW_TOP_STORES                             =   "VIEW_TOP_STORES"
        const val EVENT_GET_CUSTOM_DOMAIN                           =   "GET_CUSTOM_DOMAIN"
        const val EVENT_HELP_SCREEN_OPEN                            =   "HelpSectionClicked"
        const val EVENT_SETTINGS_REFERRAL                           =   "SettingsReferral"
        const val EVENT_PREMIUM_PAGE                                =   "Premium_Page"
        const val EVENT_GET_PREMIUM_WEBSITE                         =   "Get_Premium_Website"
        const val EVENT_APPFLYER_EXCEPTION                          =   "Appflyer_Crash_Exception"
        const val EVENT_CLERVERTAP_EXCEPTION                        =   "CleverTap_Crash_Exception"
        const val EVENT_SERVER_EXCEPTION                            =   "Server_Crash_Exception"
        const val EVENT_ADD_VARIANT                                 =   "Add_Variant"
        const val EVENT_SAVE_VARIANT                                =   "Save_Variant"
        const val EVENT_EDIT_VARIANT                                =   "Edit_Variant"
        const val EVENT_DELETE_VARIANT                              =   "Delete_Variant"
        const val EVENT_PROFILE_PAGE                                =   "ProfilePage"
        const val EVENT_SHARE_PDF_CATALOG                           =   "Share PDF Catalog"
        const val EVENT_DOMAIN_DETAIL                               =   "DomainDetails"
        const val EVENT_DOMAIN_EXPLORE                              =   "Domain_Explore"
        const val EVENT_BING_SEARCH                                 =   "Bing_Search"
        const val EVENT_SET_PREPAID_ORDER                           =   "Set_prepaid_orders"
        const val EVENT_HOW_PREPAID_ORDER_WORK                      =   "How_prepaid_orders_work"
        const val EVENT_ACTIVATE_PREPAID_ORDER                      =   "Activate_prepaid_orders"
        const val EVENT_COMPLETE_PREPAID_STEPS                      =   "Complete_prepaid_steps"
        const val EVENT_CHECK_PREPAID_ORDERS                        =   "Check_prepaid_orders"
        const val EVENT_QR_SHARED                                   =   "QRShared"
        const val EVENT_DELETE_ITEM                                 =   "DeleteItem"
        const val EVENT_DELETE_CATEGORY                             =   "DeleteCategory"
        const val EVENT_EDIT_CATEGORY                               =   "EditCategory"
        const val EVENT_TRUE_CALLER_VERIFIED                        =   "TrueCaller_OTPVerified"
        const val EVENT_SET_ONLINE_PAYMENT_MODE                     =   "Set_online_payment_modes"
        const val EVENT_SET_ORDER_DATE_SELECTION                    =   "orders_date_selected"
        const val EVENT_SET_SETTLEMENTS_DATE_SELECTION              =   "settlements_date_selected"
        const val EVENT_SET_ORDER_PAYMENT_DETAIL                    =   "order_payment_details"
        const val EVENT_VIEW_MY_PAYMENTS                            =   "View_my_payments"
        const val EVENT_CONFIRM_PAYMENTS_ACTIVATION                 =   "Confirm_payment_activation"
        const val EVENT_COMPLETE_KYC_FOR_PAYMENTS                   =   "complete_kyc_for_payments"
        const val EVENT_SET_NEW_ORDER_NOTIFICATIONS                 =   "Set_New_Order_Notifications"
        const val EVENT_NEW_ORDER_NOTIFICATION_SELECTION            =   "New_Order_Notification_Selected"
        const val EVENT_GOOGLE_ADS_EXPLORE                          =   "GA_Explore"
        const val EVENT_SEND_PAYMENT_LINK                           =   "Send_payment_link"
        const val EVENT_SEND_LINK                                   =   "Send_link"
        const val EVENT_PAYMENT_LINK_SENT                           =   "Payment_link_sent"
        const val EVENT_CREATE_COUPON                               =   "Create_Coupon"
        const val EVENT_ACTIVE_COUPON                               =   "Active_Coupon"
        const val EVENT_SHOW_COUPON                                 =   "Show_Coupon"
        const val EVENT_SHARE_COUPON                                =   "Share_coupon"
        const val EVENT_ADD_EMAIL                                   =   "Add_email"
        const val EVENT_EMAIL_SIGN_IN                               =   "Email_sign_in"
        const val EVENT_SHARE_PRODUCT_MARKETING                     =   "Share_product_marketing"
        const val EVENT_DELIVERY_SHIP_USING_PARTNER                 =   "delivery_ship_using_partner"
        const val EVENT_DELIVERY_SHIPPING_MODE                      =   "delivery_shipping_mode"
    }
}