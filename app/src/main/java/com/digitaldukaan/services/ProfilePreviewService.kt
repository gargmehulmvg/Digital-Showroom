package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.StoreLinkRequest
import com.digitaldukaan.models.request.StoreLogoRequest
import com.digitaldukaan.models.request.StoreNameRequest
import com.digitaldukaan.services.networkservice.ProfilePreviewNetworkService
import com.digitaldukaan.services.serviceinterface.IProfilePreviewServiceInterface
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfilePreviewService {

    private val mNetworkService = ProfilePreviewNetworkService()
    private lateinit var mServiceInterface: IProfilePreviewServiceInterface

    fun setServiceInterface(serviceInterface: IProfilePreviewServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getProfilePreviewData(storeId: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getProfilePreviewServerCall(storeId, mServiceInterface)
        }
    }

    fun updateStoreName(authToken: String, request: StoreNameRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateStoreNameServerCall(authToken, request, mServiceInterface)
        }
    }

    fun updateStoreLink(authToken: String, request: StoreLinkRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateStoreLinkServerCall(authToken, request, mServiceInterface)
        }
    }

    fun updateStoreLogo(authToken: String, request: StoreLogoRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateStoreLogoServerCall(authToken, request, mServiceInterface)
        }
    }

    fun generateCDNLink(authToken: String, imageType: RequestBody, file: MultipartBody.Part?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getImageUploadCdnLinkServerCall(authToken, imageType, file, mServiceInterface)
        }
    }

    fun initiateKyc(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.initiateKycServerCall(authToken, mServiceInterface)
        }
    }

}