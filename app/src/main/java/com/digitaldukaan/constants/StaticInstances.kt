package com.digitaldukaan.constants

import com.digitaldukaan.models.dto.ContactModel
import com.digitaldukaan.models.response.*

object StaticInstances {

    var sStoreInfo: StoreInfoResponse? = null
    var sBankDetails: BankDetailsResponse? = null
    var sIsStoreImageUploaded: Boolean = false
    var sUserContactList: ArrayList<ContactModel> = ArrayList()
    var sStepsCompletedList: ArrayList<StepCompletedItem>? = null
    var sHelpScreenList: ArrayList<HelpScreenItemResponse> = ArrayList()
    var sOrderPageInfoStaticData: OrderPageStaticTextResponse? = null
    var sAppSessionId: String? = ""
    var sCleverTapId: String? = ""
    var sFireBaseMessagingToken: String? = ""
    var sStoreId: Int = 0
    var sAppStoreServicesResponse: StoreServicesResponse? = null
}