package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.ExploreCategoryNetworkService
import com.digitaldukaan.services.serviceinterface.IExploreCategoryServiceInterface

class ExploreCategoryService {

    private val mNetworkService = ExploreCategoryNetworkService()
    private lateinit var mServiceInterface: IExploreCategoryServiceInterface

    fun setServiceInterface(serviceInterface: IExploreCategoryServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getMasterCategories() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getMasterCategoriesServerCall(mServiceInterface)
        }
    }

    fun getMasterSubCategories(id: Int?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            id?.let { mNetworkService.getMasterSubCategoriesServerCall(id, mServiceInterface) }
        }
    }

    fun getMasterItems(id: Int?, page: Int) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            id?.let { mNetworkService.getMasterItemsServerCall(id, page, mServiceInterface) }
        }
    }

}