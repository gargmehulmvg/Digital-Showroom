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

}