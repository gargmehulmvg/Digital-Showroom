package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.AddProductRequest
import com.digitaldukaan.models.request.DeleteItemRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IAddProductServiceInterface
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddProductNetworkService {

    suspend fun getAddOrderBottomSheetDataServerCall(
        serviceInterface: IAddProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMasterCatalogStaticText()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onAddProductBannerStaticDataResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(
                        Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
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
            serviceInterface.onAddProductException(e)
        }
    }

    suspend fun getItemInfoServerCall(
        itemId: Int,
        serviceInterface: IAddProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getItemInfo(itemId)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onGetAddProductDataResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onGetAddProductDataResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(AddProductNetworkService::class.java.simpleName, "getItemInfoServerCall: ", e)
            serviceInterface.onAddProductException(e)
        }
    }

    suspend fun setItemServerCall(
        request: AddProductRequest,
        serviceInterface: IAddProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setItem(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onAddProductDataResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onAddProductDataResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(AddProductNetworkService::class.java.simpleName, "getItemInfoServerCall: ", e)
            serviceInterface.onAddProductException(e)
        }
    }

    suspend fun convertFileToLinkServerCall(
        imageType: RequestBody,
        imageFile: MultipartBody.Part?,
        serviceInterface: IAddProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getImageUploadCdnLink(imageType, imageFile)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onConvertFileToLinkResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onConvertFileToLinkResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(AddProductNetworkService::class.java.simpleName, "convertFileToLinkServerCall: ", e)
            serviceInterface.onAddProductException(e)
        }
    }

    suspend fun deleteItemServerCall(
        request: DeleteItemRequest,
        serviceInterface: IAddProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.deleteItem(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onDeleteItemResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onDeleteItemResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "deleteCategoryServerCall: ", e)
            serviceInterface.onAddProductException(e)
        }
    }

}