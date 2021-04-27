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

    fun getProfilePreviewData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getProfilePreviewServerCall(mServiceInterface)
        }
    }

    fun updateStoreName(request: StoreNameRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateStoreNameServerCall(request, mServiceInterface)
        }
    }

    fun updateStoreLink(request: StoreLinkRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateStoreLinkServerCall(request, mServiceInterface)
        }
    }

    fun updateStoreLogo(authToken: String, request: StoreLogoRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateStoreLogoServerCall(authToken, request, mServiceInterface)
        }
    }

    fun generateCDNLink(imageType: RequestBody, file: MultipartBody.Part?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getImageUploadCdnLinkServerCall(imageType, file, mServiceInterface)
        }
    }

    fun initiateKyc(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.initiateKycServerCall(authToken, mServiceInterface)
        }
    }

    fun getShareStoreData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getShareStoreDataServerCall(mServiceInterface)
        }
    }

}