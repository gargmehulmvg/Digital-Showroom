package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.BuildCatalogRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IExploreCategoryServiceInterface
import com.google.gson.Gson

class ExploreCategoryNetworkService {

    suspend fun getMasterCategoriesServerCall(
        serviceInterface: IExploreCategoryServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMasterCategories()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let {
                        serviceInterface.onExploreCategoryResponse(it)
                    }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(
                        Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onExploreCategoryResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ExploreCategoryNetworkService::class.java.simpleName, "getMasterCategoriesServerCall: ", e)
            serviceInterface.onExploreCategoryServerException(e)
        }
    }

    suspend fun getMasterSubCategoriesServerCall(
        id: Int,
        serviceInterface: IExploreCategoryServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMasterSubCategories(id)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { apiResponse ->
                        serviceInterface.onExploreCategoryResponse(apiResponse)
                    }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onExploreCategoryResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ExploreCategoryNetworkService::class.java.simpleName, "getMasterSubCategoriesServerCall: ", e)
            serviceInterface.onExploreCategoryServerException(e)
        }
    }

    suspend fun getMasterItemsServerCall(
        id: Int,
        page: Int,
        serviceInterface: IExploreCategoryServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMasterItems(id, page)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { apiResponse ->
                        serviceInterface.onSubCategoryItemsResponse(apiResponse)
                    }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onSubCategoryItemsResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ExploreCategoryNetworkService::class.java.simpleName, "getMasterItemsServerCall: ", e)
            serviceInterface.onExploreCategoryServerException(e)
        }
    }

    suspend fun buildCatalogServerCall(
        request: BuildCatalogRequest,
        serviceInterface: IExploreCategoryServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.buildCatalog(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { apiResponse ->
                        serviceInterface.onBuildCatalogResponse(apiResponse)
                    }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onBuildCatalogResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ExploreCategoryNetworkService::class.java.simpleName, "buildCatalogServerCall: ", e)
            serviceInterface.onExploreCategoryServerException(e)
        }
    }

}