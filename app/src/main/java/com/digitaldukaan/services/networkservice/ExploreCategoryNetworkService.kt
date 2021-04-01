package com.digitaldukaan.services.networkservice

import android.util.Log
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
                    it.body()?.let {
                        serviceInterface.onExploreCategoryResponse(it)
                    }
                } else {
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

}