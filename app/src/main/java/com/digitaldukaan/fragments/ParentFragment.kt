package com.digitaldukaan.fragments

import android.net.Uri
import androidx.fragment.app.Fragment
import com.digitaldukaan.interfaces.IWebViewCallbacks
import com.digitaldukaan.models.response.LockedStoreShareResponse
import com.digitaldukaan.models.response.OrderItemResponse
import com.digitaldukaan.models.response.TransactionDetailResponse
import java.io.File

open class ParentFragment : Fragment(), IWebViewCallbacks {

    override fun onNativeBackPressed() = Unit

    override fun sendData(data: String) = Unit

    override fun showAndroidToast(data: String) = Unit

    override fun showAndroidLog(data: String) = Unit

    open fun onAlertDialogItemClicked(selectedStr: String?, id: Int, position: Int) = Unit

    open fun onDontShowDialogPositiveButtonClicked(item: OrderItemResponse?) = Unit

    open fun onImageSelectionResultFile(file: File?, mode: String = "") = Unit

    open fun onImageSelectionResultUri(fileUri: Uri?) = Unit

    open fun onImageSelectionResultFileAndUri(fileUri: Uri?, file: File?) = Unit

    open fun onNoInternetButtonClick(isNegativeButtonClick: Boolean) = Unit

    open fun onMyPaymentFragmentTabChanged() = Unit

    open fun onWhatsAppIconClicked() = Unit

    open fun refreshOrderPage() = Unit

    open fun onTransactionDetailResponse(transactionItem: TransactionDetailResponse?) = Unit

    open fun onSMSIconClicked() = Unit

    open fun onLockedStoreShareSuccessResponse(lockedShareResponse: LockedStoreShareResponse) = Unit

    open fun onSearchDialogContinueButtonClicked(inputOrderId: String, inputMobileNumber: String) = Unit

    open fun onLocationChanged(lat: Double, lng: Double) = Unit

    open fun onCheckStaffInviteResponse() = Unit

    open fun onShipmentCtaClicked(initiateServerCall: Boolean) = Unit

}