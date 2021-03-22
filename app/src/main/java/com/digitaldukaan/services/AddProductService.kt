package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.AddProductRequest
import com.digitaldukaan.models.request.ConvertFileToLinkRequest
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

    fun getItemInfo(authToken: String, itemId: Int) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getItemInfoServerCall(authToken, itemId, mServiceInterface)
        }
    }

    fun setItem(authToken: String, request: AddProductRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.setItemServerCall(authToken, request, mServiceInterface)
        }
    }

    fun convertFileToLink(authToken: String, request: ConvertFileToLinkRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.convertFileToLinkServerCall(authToken, request, mServiceInterface)
        }
    }

}