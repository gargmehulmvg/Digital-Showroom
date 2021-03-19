package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IAddProductServiceInterface
import com.google.gson.Gson

class AddProductNetworkService {

    suspend fun getAddOrderBottomSheetDataServerCall(
        authToken: String,
        serviceInterface: IAddProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMasterCatalogStaticText(authToken)
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
            serviceInterface.onAddProductException(e)
        }
    }

}