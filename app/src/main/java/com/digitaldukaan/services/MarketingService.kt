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

    fun getMarketingSuggestedDomains() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getMarketingSuggestedDomainsServerCall(mServiceInterface)
        }
    }

    fun getMarketingPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getMarketingPageInfoServerCall(mServiceInterface)
        }
    }

    fun getShareStoreData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getShareStoreDataServerCall(mServiceInterface)
        }
    }

    fun generateStorePdf() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.generateStorePdfServerCall(mServiceInterface)
        }
    }

    fun getShareStorePdfText() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getShareStorePdfTextServerCall(mServiceInterface)
        }
    }

}