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

}