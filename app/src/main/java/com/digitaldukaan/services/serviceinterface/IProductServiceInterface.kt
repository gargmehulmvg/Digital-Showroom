package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IProductServiceInterface {

    fun onAddProductBannerStaticDataResponse(commonResponse: CommonApiResponse)

    fun onProductPageInfoResponse(commonResponse: CommonApiResponse)

    fun onShareStorePdfDataResponse(commonResponse: CommonApiResponse)

    fun onProductShareStorePDFDataResponse(commonResponse: CommonApiResponse)

    fun onProductPDFGenerateResponse(commonResponse: CommonApiResponse)

    fun onProductShareStoreWAResponse(commonResponse: CommonApiResponse)

    fun onUserCategoryResponse(commonResponse: CommonApiResponse)

    fun onDeleteCategoryInfoResponse(commonResponse: CommonApiResponse)

    fun onUpdateCategoryResponse(commonResponse: CommonApiResponse)

    fun onDeleteCategoryResponse(commonResponse: CommonApiResponse)

    fun onUpdateStockResponse(commonResponse: CommonApiResponse)

    fun onQuickUpdateItemInventoryResponse(commonResponse: CommonApiResponse)

    fun onGenerateStorePdfResponse(commonResponse: CommonApiResponse)

    fun onProductException(e: Exception)

}