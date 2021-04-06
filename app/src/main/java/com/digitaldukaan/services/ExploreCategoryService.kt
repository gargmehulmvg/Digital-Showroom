package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.BuildCatalogItemRequest
import com.digitaldukaan.models.request.BuildCatalogRequest
import com.digitaldukaan.models.response.MasterCatalogItemResponse
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

    fun buildCatalog(list: ArrayList<MasterCatalogItemResponse?>) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            val buildCatalogItemList: ArrayList<BuildCatalogItemRequest> = ArrayList()
            list.forEachIndexed { _, itemResponse ->
                buildCatalogItemList.add(BuildCatalogItemRequest(itemResponse?.itemId, itemResponse?.parentCategoryIdForRequest, itemResponse?.price))
            }
            val buildCatalogRequest = BuildCatalogRequest(buildCatalogItemList)
            mNetworkService.buildCatalogServerCall(buildCatalogRequest, mServiceInterface)
        }
    }

}