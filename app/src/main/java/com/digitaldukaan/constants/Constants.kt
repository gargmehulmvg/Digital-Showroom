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
        const val SHARED_PREF_NAME = "DigitalDukaanPrefs"
        const val USER_AUTH_TOKEN = "USER_AUTH_TOKEN"
        const val STORE_ID = "STORE_ID"
        const val USER_MOBILE_NUMBER = "USER_MOBILE_NUMBER"
        const val STORE_NAME = "STORE_NAME"
        const val WEB_VIEW_HELP = "help"
        const val WEB_VIEW_NO_ORDER_PAGE = "noorderspage"
        const val WEB_VIEW_QR_DOWNLOAD = "qr"
        const val WEB_VIEW_CREATIVE_LIST = "creative-list"
        const val WEB_VIEW_SOCIAL_CREATIVE_LIST = "social-creative-list"
        const val WEB_VIEW_REWARDS = "rewards"
        const val CLIPBOARD_LABEL = "label"
        const val SETTINGS = "settings"
        const val DOTPE_OFFICIAL_URL_CLIPBOARD = "https://web.dotpe.in"
        const val DOTPE_OFFICIAL_URL = "goto : web.dotpe.in"
        //Actions
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
        //Pages
        const val PAGE_REFER = "refer"
        const val PAGE_HELP = "help"
        const val PAGE_FEEDBACK = "Feedback"
        const val PAGE_APP_SETTINGS = "Sitemap"
        const val PAGE_REWARDS = "RewardsPage"
        //ERROR_CODES
        const val ERROR_DOMAIN_ALREADY_EXISTS = "domain_already_exists"
        //GRID_LAYOUT_SPAN_TYPE
        const val SPAN_TYPE_FULL_WIDTH = "fullWidth"
        //STICKY_HEADER_VIEW_TYPE
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
        //ORDER_TYPE
        const val ORDER_TYPE_ADDRESS = 1
        const val ORDER_TYPE_PICK_UP = 2
        const val ORDER_TYPE_SELF = 3
        //DELIVERY_STATUS
        const val DS_NEW = "new"
        const val DS_SEND_BILL = "sendBill"
        const val DS_BILL_SENT = "billSent"
        const val DS_CASH_MARKED = "cashMarked"
        const val DS_PAID_ONLINE = "paidOnline"
        const val DS_REJECTED = "rejected"
        const val DS_COMPLETED_CASH = "completedCash"
        const val DS_COMPLETED_ONLINE= "completedOnline"
    }
}