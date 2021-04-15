package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.DeleteCategoryRequest
import com.digitaldukaan.models.request.UpdateCategoryRequest
import com.digitaldukaan.models.request.UpdateStockRequest
import com.digitaldukaan.services.networkservice.ProductNetworkService
import com.digitaldukaan.services.serviceinterface.IProductServiceInterface

class ProductService {

    private lateinit var mServiceInterface: IProductServiceInterface

    private val mNetworkService = ProductNetworkService()

    fun getAddOrderBottomSheetData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getAddOrderBottomSheetDataServerCall(mServiceInterface)
        }
    }

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

    fun generateProductStorePdf() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.generateProductStorePdfServerCall(mServiceInterface)
        }
    }

    fun getProductShareStoreData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getProductShareStoreDataServerCall(mServiceInterface)
        }
    }

    fun getUserCategories() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getUserCategoriesServerCall(mServiceInterface)
        }
    }

    fun getDeleteCategoryItem() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getDeleteCategoriesServerCall(mServiceInterface)
        }
    }

    fun updateCategory(request: UpdateCategoryRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateCategoryServerCall(request, mServiceInterface)
        }
    }

    fun deleteCategory(request: DeleteCategoryRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.deleteCategoryServerCall(request, mServiceInterface)
        }
    }

    fun updateStock(request: UpdateStockRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateStockServerCall(request, mServiceInterface)
        }
    }

}