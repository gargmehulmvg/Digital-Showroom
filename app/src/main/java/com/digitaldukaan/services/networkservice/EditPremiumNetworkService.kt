package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.EditPremiumColorRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IEditPremiumServiceInterface
import com.google.gson.Gson

class EditPremiumNetworkService {

    suspend fun getPremiumColorsServerCall(
        serviceInterface: IEditPremiumServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getAllPresetColors()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { apiResponse ->
                        serviceInterface.onPremiumColorsResponse(apiResponse)
                    }
                } else {
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onPremiumColorsResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(EditPremiumNetworkService::class.java.simpleName, "getPremiumPageInfoServerCall: ", e)
            serviceInterface.onEditPremiumServerException(e)
        }
    }

    suspend fun setStoreThemeColorPaletteServerCall(
        request: EditPremiumColorRequest,
        serviceInterface: IEditPremiumServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setStoreThemeColorPalette(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { apiResponse ->
                        serviceInterface.onSetPremiumColorsResponse(apiResponse)
                    }
                } else {
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onSetPremiumColorsResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(EditPremiumNetworkService::class.java.simpleName, "setStoreThemeColorPaletteServerCall: ", e)
            serviceInterface.onEditPremiumServerException(e)
        }
    }

}