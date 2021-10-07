package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.BusinessTypeRequest
import com.digitaldukaan.services.networkservice.BusinessTypeNetworkService
import com.digitaldukaan.services.serviceinterface.IBusinessTypeServiceInterface

class BusinessTypeService {

    private val mNetworkService = BusinessTypeNetworkService()
    private lateinit var mServiceInterface: IBusinessTypeServiceInterface

    fun setServiceInterface(serviceInterface: IBusinessTypeServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getBusinessListData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getBusinessListServerCall(mServiceInterface)
        }
    }

    fun setStoreBusinesses(businessTypeRequest: BusinessTypeRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.setStoreBusinessesServerCall(businessTypeRequest, mServiceInterface)
        }
    }

}