package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.MarketingNetworkService
import com.digitaldukaan.services.serviceinterface.IMarketingServiceInterface

class MarketingService {

    private lateinit var mServiceInterface : IMarketingServiceInterface

    private val mNetworkService = MarketingNetworkService()

    fun setMarketingServiceListener(listener: IMarketingServiceInterface) {
        mServiceInterface = listener
    }

    fun getMarketingCardsData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getMarketingCardsDataServerCall(mServiceInterface)
        }
    }

    fun getShareStoreData(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getShareStoreDataServerCall(authToken, mServiceInterface)
        }
    }

    fun generateStorePdf(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.generateStorePdfServerCall(authToken, mServiceInterface)
        }
    }

    fun getShareStorePdfText(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getShareStorePdfTextServerCall(authToken, mServiceInterface)
        }
    }

}