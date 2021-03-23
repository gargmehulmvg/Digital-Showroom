package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.ProductNetworkService
import com.digitaldukaan.services.serviceinterface.IProductServiceInterface

class ProductService {

    private lateinit var mServiceInterface: IProductServiceInterface

    private val mNetworkService = ProductNetworkService()

    fun setOrderDetailServiceListener(listener: IProductServiceInterface) {
        mServiceInterface = listener
    }

    fun getProductPageInfo(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getProductPageInfoServerCall(authToken, mServiceInterface)
        }
    }

    fun getProductSharePDFTextData(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getProductShareStorePdfTextServerCall(authToken, mServiceInterface)
        }
    }

    fun generateProductStorePdf(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.generateProductStorePdfServerCall(authToken, mServiceInterface)
        }
    }

    fun getProductShareStoreData(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getProductShareStoreDataServerCall(authToken, mServiceInterface)
        }
    }

}