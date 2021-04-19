package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.CreateStoreRequest
import com.digitaldukaan.services.networkservice.CreateStoreNetworkService
import com.digitaldukaan.services.serviceinterface.ICreateStoreServiceInterface

class CreateStoreService {

    private val mNetworkService = CreateStoreNetworkService()
    private lateinit var mServiceInterface: ICreateStoreServiceInterface

    fun setServiceInterface(serviceInterface: ICreateStoreServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun createStore(request: CreateStoreRequest?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.createStoreServerCall(request, mServiceInterface)
        }
    }

}