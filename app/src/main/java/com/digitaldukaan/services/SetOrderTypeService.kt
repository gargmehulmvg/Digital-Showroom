package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.SetOrderTypeNetworkService
import com.digitaldukaan.services.serviceinterface.ISetOrderTypeServiceInterface

class SetOrderTypeService {

    private val mNetworkService = SetOrderTypeNetworkService()
    private lateinit var mServiceInterface: ISetOrderTypeServiceInterface

    fun setServiceInterface(serviceInterface: ISetOrderTypeServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getOrderTypePageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getOrderTypePageInfoServerCall(mServiceInterface)
        }
    }

}