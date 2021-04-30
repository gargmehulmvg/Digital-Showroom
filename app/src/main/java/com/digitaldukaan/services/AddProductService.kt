package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.AddProductRequest
import com.digitaldukaan.models.request.DeleteItemRequest
import com.digitaldukaan.services.networkservice.AddProductNetworkService
import com.digitaldukaan.services.serviceinterface.IAddProductServiceInterface
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddProductService {

    private lateinit var mServiceInterface: IAddProductServiceInterface

    private val mNetworkService = AddProductNetworkService()

    fun setServiceListener(listener: IAddProductServiceInterface) {
        mServiceInterface = listener
    }

    fun getAddOrderBottomSheetData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getAddOrderBottomSheetDataServerCall(mServiceInterface)
        }
    }

    fun getItemInfo(itemId: Int) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getItemInfoServerCall(itemId, mServiceInterface)
        }
    }

    fun setItem(authToken: String, request: AddProductRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.setItemServerCall(authToken, request, mServiceInterface)
        }
    }

    fun generateCDNLink(imageType: RequestBody, file: MultipartBody.Part?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.convertFileToLinkServerCall(imageType, file, mServiceInterface)
        }
    }

    fun deleteItemServerCall(request: DeleteItemRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.deleteItemServerCall(request, mServiceInterface)
        }
    }

}