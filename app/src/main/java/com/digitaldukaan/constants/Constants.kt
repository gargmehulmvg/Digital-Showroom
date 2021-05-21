package com.digitaldukaan.constants

class Constants {
    companion object {
        const val SPLASH_TIMER = 1_000L
        const val LOCATION_REQUEST_CODE = 1001
        const val BACK_PRESS_INTERVAL = 2_000L
        const val SHINE_ANIMATION_INTERVAL = 2_000L
        const val STORE_CREATION_PROGRESS_ANIMATION_INTERVAL = 3_000L
        const val RESEND_OTP_TIMER = 30_000L
        const val TIMER_INTERVAL = 1_000L
        const val CREDENTIAL_PICKER_REQUEST = 1001
        const val IMAGE_PICK_REQUEST_CODE = 1002
        const val CONTACT_REQUEST_CODE = 1003
        const val CROP_IMAGE_REQUEST_CODE = 1004
        const val EXTERNAL_STORAGE_REQUEST_CODE = 1005
        const val ERROR_CODE_UN_AUTHORIZED_ACCESS = 401
        const val ERROR_MESSAGE_UN_AUTHORIZED_ACCESS = "unauthorized access"
        const val SHARED_PREF_NAME = "DigitalDukaanPrefs"
        const val USER_AUTH_TOKEN = "USER_AUTH_TOKEN"
        const val STORE_ID = "STORE_ID"
        const val USER_ID = "USER_ID"
        const val USER_MOBILE_NUMBER = "USER_MOBILE_NUMBER"
        const val STORE_NAME = "STORE_NAME"
        const val CLIPBOARD_LABEL = "label"
        const val SETTINGS = "settings"
        const val DOTPE_OFFICIAL_URL_CLIPBOARD = "https://web.dotpe.in"
        const val DOTPE_OFFICIAL_URL = "Go to web.dotpe.in"
        const val KEY_DONT_SHOW_MESSAGE_AGAIN = "KEY_DONT_SHOW_MESSAGE_AGAIN"
        const val KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK = "KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK"
        const val TEXT_YES = "yes"
        const val TEXT_NO = "no"
        const val APP_SECRET_KEY = "wFqjaY2jR85nMmgI7wqvPHd6mYaEN9a1"
        //ACTIONS
        const val ACTION_LOGOUT = "logout"
        const val ACTION_STORE_DESCRIPTION = "store_description"
        const val ACTION_BANK_ACCOUNT = "bank_account"
        const val ACTION_BUSINESS_TYPE = "bussiness_type"
        const val ACTION_EDIT_STORE_LINK = "edit_store_link"
        const val ACTION_STORE_NAME = "store_name"
        const val ACTION_STORE_LOCATION = "store_location"
        const val ACTION_BUSINESS_CREATIVE = "business_creatives"
        const val ACTION_SOCIAL_CREATIVE = "social_creatives"
        const val ACTION_QR_DOWNLOAD = "qr_download"
        const val ACTION_SHARE_DATA = "share_text"
        const val ACTION_CATALOG_WHATSAPP = "catalog_whatsapp"
        const val ACTION_CATALOG_PREMIUM = "theme-discover"
        const val ACTION_KYC_STATUS = "kyc_status"
        const val ACTION_ADD_BANK = "add-bank"
        const val ACTION_REJECT_ORDER = "reject-order"
        const val ACTION_SHARE_BILL = "share-bill"
        const val ACTION_DOWNLOAD_BILL = "download-bill"
        const val ACTION_CASH_COLLECTED = "cash-collected"
        const val ACTION_KYC = "kyc"
        const val ACTION_DELIVERY_CHARGES = "delivery-charges"
        //Incomplete profile actions
        const val ACTION_LOGO = "logo"
        const val ACTION_DESCRIPTION = "description"
        const val ACTION_LOCATION = "address"
        const val ACTION_BUSINESS = "business"
        const val ACTION_BANK = "bank"
        //PAGES
        const val PAGE_REFER = "refer"
        const val PAGE_HELP = "help"
        const val PAGE_FEEDBACK = "Feedback"
        const val PAGE_APP_SETTINGS = "Sitemap"
        const val PAGE_REWARDS = "RewardsPage"
        //ERROR_CODES
        const val ERROR_DOMAIN_ALREADY_EXISTS = "domain_already_exists"
        //GRID_LAYOUT_SPAN_TYPE
        const val SPAN_TYPE_FULL_WIDTH = "fullWidth"
        //ORDER_TYPE
        const val ORDER_TYPE_ADDRESS = 1
        const val ORDER_TYPE_PICK_UP = 2
        const val ORDER_TYPE_SELF = 3
        const val ORDER_TYPE_SELF_IMAGE = 4
        //DISPLAY_STATUS
        const val DS_NEW = "new"                                // When order comes but not opened by merchant
        const val DS_SEND_BILL = "sendBill"                     // When order comes opened the order but didn't send bill and exit
        const val DS_BILL_SENT = "billSent"                     // When order comes and send bill but payment is due
        const val DS_CASH_MARKED = "cashMarked"                 // When order comes & marked as cash
        const val DS_PAID_ONLINE = "paidOnline"                 // When order comes & order is paid online by customer
        const val DS_REJECTED = "rejected"                      // Order rejected byt customer
        const val DS_COMPLETED_CASH = "completedCash"           // When order comes & marked as cash
        const val DS_COMPLETED_ONLINE= "completedOnline"        // Order self checklist by merchant :: Paid Online
        //ORDER_PAGE
        const val MODE_PENDING = "mode_pending"
        const val MODE_COMPLETED = "mode_completed"
        //ORDER_STATUS
        const val StatusCustomerUpdated = 1
        const val StatusMerchantUpdated = 2
        const val StatusRejected = 3 //(Status_message : Merchant/Customer Rejected)
        const val StatusActive = 4 //(Accepted Tab in merchant)
        const val StatusReady = 5 //(For delivery / pickup)
        const val StatusOutForDelivery = 6
        const val StatusCompleted = 7
        const val StatusCancelled = 8
        const val StatusSeenByMerchant = 9
        //BASE64 APIs END POINT
        const val BASE64_STORE_LOGO = "store_logo"
        const val BASE64_STORE_ITEMS = "store_items"
        const val BASE64_ORDER_BILL = "order_bill"
        const val BASE64_THEMES = "themes"
        //CREATOR_TYPE
        const val CREATOR_TYPE_MERCHANT = 1
        const val CREATOR_TYPE_CUSTOMER = 2
        //NEW RELEASE TYPES
        const val NEW_RELEASE_TYPE_WEBVIEW = "webview"
        const val NEW_RELEASE_TYPE_PREMIUM = "premium"
        const val NEW_RELEASE_TYPE_NEW = "new"
        const val NEW_RELEASE_TYPE_CUSTOM_DOMAIN = "custom-domain"
        const val NEW_RELEASE_TYPE_PREPAID_ORDER = "prepaid-order"
        const val NEW_RELEASE_TYPE_TRENDING = "trending"
        const val NEW_RELEASE_TYPE_EXTERNAL = "external"
        //EDIT PHOTO MODES
        const val EDIT_PHOTO_MODE_MOBILE = "mobile"
        const val EDIT_PHOTO_MODE_DESKTOP = "desktop"
        //MODES
        const val MODE_CROP = "crop"
        const val MODE_ADD_PRODUCT = "add_product"
        const val MODE_PRODUCT_LIST = "product_list"
        //MODE ORDER STATUS
        const val ORDER_STATUS_SUCCESS = "Success"
        const val ORDER_STATUS_REJECTED = "Rejected"
        const val ORDER_STATUS_IN_PROGRESS = "In-Progress"
        const val ORDER_STATUS_REFUND_SUCCESS  = "refund-success"
        const val ORDER_STATUS_PAYOUT_SUCCESS  = "payout-success"
        const val ORDER_STATUS_LOCKED = "Locked"
        //MODE DELIVERY CHARGE
        const val UNKNOWN_DELIVERY_CHARGE = 0
        const val FREE_DELIVERY = 1
        const val FIXED_DELIVERY_CHARGE = 2
        const val CUSTOM_DELIVERY_CHARGE = 3
        //ITEM_TYPE_CHARGE
        const val ITEM_TYPE_DELIVERY_CHARGE = "delivery_charge"
        const val ITEM_TYPE_CHARGE = "charge"
        const val ITEM_TYPE_DISCOUNT = "discount"
    }
}