package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.StoreDeliveryStatusChangeRequest
import com.digitaldukaan.services.networkservice.ProfileNetworkService
import com.digitaldukaan.services.serviceinterface.IProfileServiceInterface

class ProfileService {

    private val mNetworkService = ProfileNetworkService()
    private lateinit var mProfileServiceInterface: IProfileServiceInterface

    fun setProfileServiceInterface(serviceInterface: IProfileServiceInterface) {
        mProfileServiceInterface = serviceInterface
    }

    fun getUserProfile(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getProfileServerCall(authToken, mProfileServiceInterface)
        }
    }

    fun changeStoreAndDeliveryStatus(authToken : String, request: StoreDeliveryStatusChangeRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.changeStoreAndDeliveryStatusServerCall(authToken ,request, mProfileServiceInterface)
        }
    }

    fun getReferAndEarnData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getReferAndEarnDataServerCall(mProfileServiceInterface)
        }
    }

    fun getReferAndEarnDataOverWhatsApp() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getReferAndEarnDataForWhatsAppServerCall(mProfileServiceInterface)
        }
    }
}