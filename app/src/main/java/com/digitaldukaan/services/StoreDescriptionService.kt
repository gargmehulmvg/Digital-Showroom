package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.StoreDescriptionRequest
import com.digitaldukaan.services.networkservice.StoreDescriptionNetworkService
import com.digitaldukaan.services.serviceinterface.IStoreDescriptionServiceInterface

class StoreDescriptionService {

    private val mNetworkService = StoreDescriptionNetworkService()
    private lateinit var mServiceInterface: IStoreDescriptionServiceInterface

    fun setServiceInterface(serviceInterface: IStoreDescriptionServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun saveStoreDescriptionData(authToken: String, request:StoreDescriptionRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.saveStoreDescriptionServerCall(authToken, request, mServiceInterface)
        }
    }

}