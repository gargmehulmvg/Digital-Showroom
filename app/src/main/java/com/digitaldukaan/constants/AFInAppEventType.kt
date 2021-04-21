package com.digitaldukaan.constants

interface AFInAppEventType {

    companion object {
        const val EVENT_SET_MIN_ORDER_VALUE = "Set_minimum_order_value"
        const val EVENT_SET_MIN_ORDER_VALUE_SAVE = "minimum_order_value_saved"
        const val EVENT_SET_DELIVERY_CHARGE = "Set_delivery_charge"
        const val EVENT_MARKET_VIEW_NOW = "Mkt_View_Now"
        const val EVENT_MARKET_START_NOW = "StartNow"
        const val EVENT_MARKET_GET_OTP = "GetOTP"
        const val EVENT_MARKET_OTP_VERIFIED = "OTPVerified"
        const val EVENT_MARKET_ENTER_NAME = "Enter Name"
        const val EVENT_VERIFY_LOCATION_SET = "LocationSet"
        const val EVENT_VERIFY_ORDER_SEEN = "OrderSeen"
        const val EVENT_GENERATE_SELF_BILL = "Generate Self Bill"
        const val EVENT_ADD_ITEM = "AddItem"
        const val EVENT_EDIT_STORE_LINK = "Edit_Store_Link"
        const val EVENT_RE_ARRANGE_PAGE_OPEN = "Rearrange section open"
        const val EVENT_STORE_SHARE = "Store Share"
        const val EVENT_UPDATE_STORE_NAME = "Update Store Name"
        const val EVENT_BUSINESS_TYPE_SELECT = "Business Type Select"
        const val EVENT_DELIVERY_MODEL_SELECT = "Delivery_model_select"
        const val EVENT_BULK_UPLOAD_ITEMS = "Bulk_Upload_Items"
        const val EVENT_HELP_SCREEN_OPEN = "HelpSectionClicked"
        const val EVENT_SETTINGS_REFERRAL = "SettingsReferral"
    }
}