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

    fun generateCDNLink(authToken: String, imageType: RequestBody, file: MultipartBody.Part?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.convertFileToLinkServerCall(authToken, imageType, file, mServiceInterface)
        }
    }

    fun updateOrder(authToken: String, request: UpdateOrderRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateOrderServerCall(authToken, request, mServiceInterface)
        }
    }

}