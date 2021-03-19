package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.AddProductNetworkService
import com.digitaldukaan.services.serviceinterface.IAddProductServiceInterface

class AddProductService {

    private lateinit var mServiceInterface: IAddProductServiceInterface

    private val mNetworkService = AddProductNetworkService()

    fun setServiceListener(listener: IAddProductServiceInterface) {
        mServiceInterface = listener
    }

    fun getAddOrderBottomSheetData(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getAddOrderBottomSheetDataServerCall(authToken, mServiceInterface)
        }
    }

}