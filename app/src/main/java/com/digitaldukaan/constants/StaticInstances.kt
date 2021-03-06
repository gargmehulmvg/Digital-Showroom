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
    var sAccountPageSettingsStaticData: AccountStaticTextResponse? = null
    var sSearchDomainUrl: String? = ""
    var sExploreDomainUrl: String? = ""
    var sAppSessionId: String? = ""
    var sCleverTapId: String? = ""
    var sFireBaseMessagingToken: String? = ""
    var sFireBaseAppInstanceId: String? = ""
    var sIsShareStoreLocked: Boolean = false
    var sAppStoreServicesResponse: StoreServicesResponse? = null
    var sPaymentMethodStr: String? = null
    var sAppFlyerRefMobileNumber: String? = ""
    var sStaticData: StaticTextResponse? = null
    var sMyPaymentPageInfoResponse: MyPaymentsPageInfoResponse? = null
    var sCustomDomainBottomSheetResponse: CustomDomainBottomSheetResponse? = null
    var sSuggestedDomainsList: ArrayList<PrimaryDomainItemResponse>? = null
    var sSuggestedDomainsListFetchedFromServer = false
    var sStaffInvitation: StaffInvitationResponse? = null
    var sPermissionHashMap: HashMap<String, Boolean>? = null
    var sMerchantMobileNumber: String = ""
}