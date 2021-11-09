package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.PrimaryDomainItemResponse
import com.digitaldukaan.models.response.ZeroOrderItemsResponse

interface ILandingPageAdapterListener {

    fun onLandingPageAdapterIsPrimaryDetected(position: Int, item: ZeroOrderItemsResponse?)

    fun onLandingPageAdapterCustomDomainApplyItemClicked(item: PrimaryDomainItemResponse?)

    fun onLandingPageAdapterAddProductItemClicked()

    fun onLandingPageAdapterCtaClicked(item: ZeroOrderItemsResponse?)

    fun onLandingPageCustomCtaClicked(url: String?)

}