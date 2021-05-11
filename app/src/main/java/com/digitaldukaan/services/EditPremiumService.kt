package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.EditPremiumColorRequest
import com.digitaldukaan.services.networkservice.EditPremiumNetworkService
import com.digitaldukaan.services.serviceinterface.IEditPremiumServiceInterface

class EditPremiumService {

    private val mNetworkService = EditPremiumNetworkService()
    private lateinit var mServiceInterface: IEditPremiumServiceInterface

    fun setServiceInterface(serviceInterface: IEditPremiumServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getPremiumColors() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getPremiumColorsServerCall(mServiceInterface)
        }
    }

    fun setStoreThemeColorPalette(storeThemeId: Int?, selectedColorId: Int?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            val request = EditPremiumColorRequest(storeThemeId, selectedColorId)
            mNetworkService.setStoreThemeColorPaletteServerCall(request, mServiceInterface)
        }
    }

}