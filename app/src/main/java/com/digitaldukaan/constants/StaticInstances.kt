package com.digitaldukaan.constants

import com.digitaldukaan.models.dto.ContactModel
import com.digitaldukaan.models.response.BankDetailsResponse
import com.digitaldukaan.models.response.OrderPageStaticTextResponse
import com.digitaldukaan.models.response.StepCompletedItem
import com.digitaldukaan.models.response.StoreInfoResponse

object StaticInstances {

    var sStoreInfo: StoreInfoResponse? = null
    var sBankDetails: BankDetailsResponse? = null
    var sIsStoreImageUploaded: Boolean = false
    var sUserContactList: MutableList<ContactModel> = ArrayList()
    var sStepsCompletedList: ArrayList<StepCompletedItem>? = null
    var sOrderPageInfoStaticData: OrderPageStaticTextResponse? = null
    var sAppSessionId: String? = ""
}