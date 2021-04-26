package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.StoreAddressRequest
import com.digitaldukaan.services.networkservice.StoreAddressNetworkService
import com.digitaldukaan.services.serviceinterface.IStoreAddressServiceInterface

class StoreAddressService {

    private val mNetworkService = StoreAddressNetworkService()
    private lateinit var mServiceInterface: IStoreAddressServiceInterface

    fun setServiceInterface(serviceInterface: IStoreAddressServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun updateStoreAddress(request: StoreAddressRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateStoreAddressServerCall(request, mServiceInterface)
        }
    }

}