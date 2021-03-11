package com.digitaldukaan.constants

import com.digitaldukaan.models.request.ContactModel
import com.digitaldukaan.models.response.BankDetailsResponse
import com.digitaldukaan.models.response.StoreInfoResponse

object StaticInstances {

    var sStoreInfo: StoreInfoResponse? = null
    var sBankDetails: BankDetailsResponse? = null
    var sIsStoreImageUploaded: Boolean = false
    var sUserContactList: MutableList<ContactModel> = ArrayList()
}