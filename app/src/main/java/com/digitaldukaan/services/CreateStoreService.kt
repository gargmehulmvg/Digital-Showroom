package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.CreateDukaanNetworkService
import com.digitaldukaan.services.serviceinterface.ICreateStoreServiceInterface

class CreateStoreService {

    private val mNetworkService = CreateDukaanNetworkService()
    private lateinit var mServiceInterface: ICreateStoreServiceInterface

    fun setServiceInterface(serviceInterface: ICreateStoreServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getCustomDomainBottomSheetData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getCustomDomainBottomSheetDataServerCall(mServiceInterface)
        }
    }
}