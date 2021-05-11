package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.DeleteCategoryRequest
import com.digitaldukaan.models.request.UpdateCategoryRequest
import com.digitaldukaan.models.request.UpdateStockRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IProductServiceInterface
import com.google.gson.Gson

class ProductNetworkService {

    suspend fun getAddOrderBottomSheetDataServerCall(
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMasterCatalogStaticText()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onAddProductBannerStaticDataResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onAddProductBannerStaticDataResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(AddProductNetworkService::class.java.simpleName, "getAddOrderBottomSheetDataServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun getProductPageInfoServerCall(
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProductPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onProductResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onProductResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "getProductPageInfoServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun generateStorePdfServerCall(
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.generateStorePdf()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onGenerateStorePdfResponse(responseBody) }
                } else serviceInterface.onProductException(Exception(response.message()))
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getShareStoreDataServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun getShareStorePdfTextServerCall(
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getShareStorePdfText()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onShareStorePdfDataResponse(responseBody) }
                } else serviceInterface.onProductException(Exception(response.message()))
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getShareStoreDataServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun generateProductStorePdfServerCall(
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.generateProductStorePdf()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onProductPDFGenerateResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onProductPDFGenerateResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "generateProductStorePdfServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun getProductShareStoreDataServerCall(
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProductShareStoreData()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onProductShareStoreWAResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onProductShareStoreWAResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "getProductShareStoreDataServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun getUserCategoriesServerCall(
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getUserCategories()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onUserCategoryResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onUserCategoryResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "getUserCategoriesServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun getDeleteCategoriesServerCall(
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getDeleteCategoryInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onDeleteCategoryInfoResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onDeleteCategoryInfoResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "getDeleteCategoriesServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun updateCategoryServerCall(
        request: UpdateCategoryRequest,
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateCategory(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onUpdateCategoryResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onUpdateCategoryResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "updateCategoryServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun deleteCategoryServerCall(
        request: DeleteCategoryRequest,
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.deleteCategory(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onDeleteCategoryResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onDeleteCategoryResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "deleteCategoryServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun updateStockServerCall(
        request: UpdateStockRequest,
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateStock(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onUpdateStockResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onUpdateStockResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "updateStockServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

}