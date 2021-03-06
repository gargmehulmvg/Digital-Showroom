package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.CreateCouponsRequest
import com.digitaldukaan.models.request.GetPromoCodeRequest
import com.digitaldukaan.services.networkservice.CustomCouponsNetworkService
import com.digitaldukaan.services.serviceinterface.ICustomCouponsServiceInterface
import com.digitaldukaan.services.serviceinterface.IPromoCodePageInfoServiceInterface

class CustomCouponsService {

    private lateinit var mServiceInterface : ICustomCouponsServiceInterface
    private lateinit var mPromoPageInfoServiceInterface : IPromoCodePageInfoServiceInterface

    private val mNetworkService = CustomCouponsNetworkService()

    fun setPromoPageInfoServiceListener(listener: IPromoCodePageInfoServiceInterface) {
        mPromoPageInfoServiceInterface = listener
    }

    fun setCustomCouponsServiceListener(listener: ICustomCouponsServiceInterface) {
        mServiceInterface = listener
    }

    fun getCreatePromoCode(request: CreateCouponsRequest?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            request?.let { req -> mNetworkService.getCreatePromoCodeServerCall(mServiceInterface, req) }
        }
    }

    fun getAllMerchantPromoCodes(request: GetPromoCodeRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getAllMerchantPromoCodesServerCall(mPromoPageInfoServiceInterface, request)
        }
    }

    fun getCouponDetails(promoCode: String?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            promoCode?.let { str -> mNetworkService.getCouponDetailsServerCall(str, mPromoPageInfoServiceInterface) }
        }
    }

    fun shareCoupon(promoCode: String?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            promoCode?.let { str -> mNetworkService.shareCouponServerCall(str, mPromoPageInfoServiceInterface) }
        }
    }

    fun getPromoCodePageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getPromoCodePageInfoServerCall(mPromoPageInfoServiceInterface)
        }
    }

    fun getPromoCodePageInfoV2() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getPromoCodePageInfoServerCallV2(mServiceInterface)
        }
    }

}