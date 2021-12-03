package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.CreateStoreRequest
import com.digitaldukaan.services.networkservice.DukaanNameNetworkService
import com.digitaldukaan.services.serviceinterface.ICreateStoreServiceInterface

class DukaanNameService {

    private val mNetworkService = DukaanNameNetworkService()
    private lateinit var mServiceInterface: ICreateStoreServiceInterface

    fun setServiceInterface(serviceInterface: ICreateStoreServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun createStore(request: CreateStoreRequest?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.createStoreServerCall(request, mServiceInterface)
        }
    }

    /*fun checkStaffInvite() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.checkStaffInviteServerCall(mServiceInterface)
        }
    }*/
}