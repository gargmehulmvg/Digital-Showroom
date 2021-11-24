package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.StoreLinkRequest
import com.digitaldukaan.models.request.StoreLogoRequest
import com.digitaldukaan.models.request.StoreNameRequest
import com.digitaldukaan.models.request.StoreUserMailDetailsRequest
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

    fun updateStoreLogo(request: StoreLogoRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateStoreLogoServerCall(request, mServiceInterface)
        }
    }

    fun generateCDNLink(imageType: RequestBody, file: MultipartBody.Part?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getImageUploadCdnLinkServerCall(imageType, file, mServiceInterface)
        }
    }

    fun initiateKyc() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.initiateKycServerCall(mServiceInterface)
        }
    }

    fun getShareStoreData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getShareStoreDataServerCall(mServiceInterface)
        }
    }

    fun getStoreUserPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getStoreUserPageInfoServerCall(mServiceInterface)
        }
    }

    fun setStoreUserGmailDetails(request: StoreUserMailDetailsRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.setStoreUserGmailDetailsServerCall(request, mServiceInterface)
        }
    }

    fun setGST(text: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.setGSTServerCall(text, mServiceInterface)
        }
    }

}