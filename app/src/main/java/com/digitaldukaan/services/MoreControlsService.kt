package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.MoreControlsRequest
import com.digitaldukaan.services.networkservice.MoreControlNetworkService
import com.digitaldukaan.services.serviceinterface.IMoreControlsServiceInterface

class MoreControlsService {

    private val mNetworkService = MoreControlNetworkService()
    private lateinit var mServiceInterface: IMoreControlsServiceInterface

    fun setServiceInterface(serviceInterface: IMoreControlsServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun updateDeliveryInfo(authToken: String, request: MoreControlsRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateDeliveryInfoServerCall(authToken, request, mServiceInterface)
        }
    }

}