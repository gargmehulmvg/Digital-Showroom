package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.SocialMediaTemplateFavouriteRequest
import com.digitaldukaan.services.networkservice.SocialMediaNetworkService
import com.digitaldukaan.services.serviceinterface.ISocialMediaServiceInterface

class SocialMediaService {

    private val mNetworkService = SocialMediaNetworkService()

    private lateinit var mServiceInterface: ISocialMediaServiceInterface

    fun setSocialMediaServiceInterface(serviceInterface: ISocialMediaServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getSocialMediaPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getSocialMediaPageInfoServerCall(mServiceInterface)
        }
    }

    fun getSocialMediaTemplateList(id: String, page: Int) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getSocialMediaTemplateListServerCall(id = id, page = page, serviceInterface = mServiceInterface)
        }
    }

    fun setSocialMediaFavourite(request: SocialMediaTemplateFavouriteRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.setSocialMediaFavouriteServerCall(request = request, serviceInterface = mServiceInterface)
        }
    }

}