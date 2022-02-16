package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.LeadsListRequest
import com.digitaldukaan.services.networkservice.LeadsSearchNetworkService
import com.digitaldukaan.services.serviceinterface.ILeadsSearchServiceInterface

class LeadsSearchService {

    private lateinit var mServiceInterface: ILeadsSearchServiceInterface

    private val mNetworkService = LeadsSearchNetworkService()

    fun setServiceListener(listener: ILeadsSearchServiceInterface) {
        mServiceInterface = listener
    }

    fun getCartsByFilters(request: LeadsListRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getCartsByFiltersServerCall(mServiceInterface, request)
        }
    }

}