package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IEditSocialMediaTemplateServiceInterface
import com.google.gson.Gson

class EditSocialMediaTemplateNetworkService {

    suspend fun getItemsBasicDetailsByStoreIdServerCall(serviceInterface: IEditSocialMediaTemplateServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getItemsBasicDetailsByStoreId()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> serviceInterface.onItemsBasicDetailsByStoreIdResponse(generateOtpResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    serviceInterface.onEditSocialMediaTemplateErrorResponse(Exception(response.message()))
                }
            }
        } catch (e : Exception) {
            Log.e(EditSocialMediaTemplateNetworkService::class.java.simpleName, "getItemsBasicDetailsByStoreIdServerCall: ", e)
            serviceInterface.onEditSocialMediaTemplateErrorResponse(e)
        }
    }

    suspend fun getUserCategoriesServerCall(serviceInterface: IEditSocialMediaTemplateServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProductsCategories()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onProductCategoryResponse(commonApiResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onProductCategoryResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(EditSocialMediaTemplateNetworkService::class.java.simpleName, "getUserCategoriesServerCall: ", e)
            serviceInterface.onEditSocialMediaTemplateErrorResponse(e)
        }
    }

    suspend fun getSocialMediaTemplateBackgroundsServerCall(id: String, serviceInterface: IEditSocialMediaTemplateServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getSocialMediaTemplateBackgrounds(id)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onSocialMediaTemplateBackgroundsResponse(commonApiResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onSocialMediaTemplateBackgroundsResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(EditSocialMediaTemplateNetworkService::class.java.simpleName, "getSocialMediaTemplateBackgroundsServerCall: ", e)
            serviceInterface.onEditSocialMediaTemplateErrorResponse(e)
        }
    }

}