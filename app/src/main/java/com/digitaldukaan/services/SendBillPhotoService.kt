package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.UpdateOrderRequest
import com.digitaldukaan.services.networkservice.SendBillPhotoNetworkService
import com.digitaldukaan.services.serviceinterface.ISendBillPhotoServiceInterface
import okhttp3.MultipartBody
import okhttp3.RequestBody

class SendBillPhotoService {

    private lateinit var mServiceInterface: ISendBillPhotoServiceInterface

    private val mNetworkService = SendBillPhotoNetworkService()

    fun setServiceListener(listener: ISendBillPhotoServiceInterface) {
        mServiceInterface = listener
    }

    fun generateCDNLink(imageType: RequestBody, file: MultipartBody.Part?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.convertFileToLinkServerCall(imageType, file, mServiceInterface)
        }
    }

    fun updateOrder(request: UpdateOrderRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateOrderServerCall(request, mServiceInterface)
        }
    }

}