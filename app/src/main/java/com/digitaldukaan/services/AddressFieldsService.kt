package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.AddressFieldsNetworkService
import com.digitaldukaan.services.serviceinterface.IAddressFieldsServiceInterface

class AddressFieldsService {

    private val mNetworkService = AddressFieldsNetworkService()
    private lateinit var mServiceInterface: IAddressFieldsServiceInterface

    fun setServiceInterface(serviceInterface: IAddressFieldsServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getAddressFieldsPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getAddressFieldsPageInfoServerCall(mServiceInterface)
        }
    }

}