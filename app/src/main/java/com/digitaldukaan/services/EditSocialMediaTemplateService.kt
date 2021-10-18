package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.EditSocialMediaTemplateNetworkService
import com.digitaldukaan.services.serviceinterface.IEditSocialMediaTemplateServiceInterface

class EditSocialMediaTemplateService {

    private lateinit var mServiceInterface : IEditSocialMediaTemplateServiceInterface

    private val mNetworkService = EditSocialMediaTemplateNetworkService()

    fun setEditSocialMediaTemplateServiceListener(listener: IEditSocialMediaTemplateServiceInterface) {
        mServiceInterface = listener
    }

    fun getItemsBasicDetailsByStoreId() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getItemsBasicDetailsByStoreIdServerCall(mServiceInterface)
        }
    }

    fun getProductCategories() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getUserCategoriesServerCall(mServiceInterface)
        }
    }

}