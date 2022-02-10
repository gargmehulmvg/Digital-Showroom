package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.AbandonedCartReminderRequest
import com.digitaldukaan.services.networkservice.LeadsDetailNetworkService
import com.digitaldukaan.services.serviceinterface.ILeadsDetailServiceInterface

class LeadsDetailService {

    private lateinit var mServiceInterface: ILeadsDetailServiceInterface

    private val mNetworkService = LeadsDetailNetworkService()

    fun setServiceListener(listener: ILeadsDetailServiceInterface) {
        mServiceInterface = listener
    }

    fun getOrderCartById(id: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getOrderCartByIdServerCall(mServiceInterface, id)
        }
    }

    fun sendAbandonedCartReminder(request: AbandonedCartReminderRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.sendAbandonedCartReminderServerCall(mServiceInterface, request)
        }
    }

}