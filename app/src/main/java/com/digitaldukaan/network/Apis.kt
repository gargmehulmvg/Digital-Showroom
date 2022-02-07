package com.digitaldukaan.network

import com.digitaldukaan.models.request.*
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.GenerateOtpResponse
import com.digitaldukaan.models.response.ImagesSearchResponse
import com.digitaldukaan.models.response.ReferEarnOverWhatsAppResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface Apis {

    /* ----------------------       On-Boarding         ---------------------- */
    @GET("api/dotk/vo1/onboard/getAppVersion")
    suspend fun getAppVersion(@Query("app_name") appName: String, @Query("app_version") appVersion: String) : Response<CommonApiResponse>

    @GET("api/dotk/vo1/onboard/getAppStaticText")
    suspend fun getAppStaticText(@Query("language_id") languageId:String) : Response<CommonApiResponse>

    @POST("api/dotk/vc1/onboard/createStore")
    suspend fun createStore(@Body request: CreateStoreRequest?): Response<CommonApiResponse>

    @GET("api/dotk/vo1/onboard/getLoginHelpScreen")
    suspend fun getHelpScreens(): Response<CommonApiResponse>

    @POST("api/dotk/vo1/onboard/validateUser")
    suspend fun validateUser(@Body request: ValidateUserRequest): Response<CommonApiResponse>

    @GET("api/dotk/vo1/onboard/getOtpModesList")
    suspend fun getOtpModesList() : Response<CommonApiResponse>

    @POST("api/dotk/vm1/onboard/setStoreAddress")
    suspend fun updateStoreAddress(@Body request: StoreAddressRequest) : Response<CommonApiResponse>

    /* ----------------------         User             ---------------------- */
    @GET("api/dotk/vc1/user/getRequestPermissionText/{id}")
    suspend fun getRequestPermissionText(@Path("id") id: Int) : Response<CommonApiResponse>

    @GET("api/dotk/vc1/user/checkStaffInvite")
    suspend fun checkStaffInvite() : Response<CommonApiResponse>

    @POST("api/dotk/vo1/user/generateOtp/{phoneNumber}")
    suspend fun generateOTP(@Path("phoneNumber") phoneNumber: String, @Body request: GenerateOtpRequest) : Response<GenerateOtpResponse>

    @POST("api/dotk/vo1/user/loginV2")
    suspend fun validateOTP(@Body request: ValidateOtpRequest) : Response<CommonApiResponse>

    @POST("api/dotk/vc1/user/updateInvitationStatus")
    suspend fun updateInvitationStatus(@Body request: UpdateInvitationRequest) : Response<CommonApiResponse>

    @GET("api/dotk/vc1/user/getStaffMemberDetails/{storeId}")
    suspend fun getStaffMembersDetails(@Path("storeId") storeId: String) : Response<CommonApiResponse>

    /* ----------------------       Settings         ---------------------- */
    @GET("api/dotk/vm1/settings/getOrderTypePageInfo")
    suspend fun getOrderTypePageInfo() : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setGST")
    suspend fun setGST(@Body request: SetGstRequest) : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/createReseller")
    suspend fun createReseller(@Body request: CreateResellerRequest) : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/verifyDisplayPhoneNumber")
    suspend fun verifyDisplayPhoneNumber(@Body request: VerifyDisplayPhoneNumberRequest) : Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getStoreLocation")
    suspend fun getStoreLocation() : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/getTransactionsList")
    suspend fun getTransactionsList(@Body request: TransactionRequest) : Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getMyPaymentsPageInfo")
    suspend fun getMyPaymentsPageInfo() : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/updatePaymentMethod")
    suspend fun updatePaymentMethod(@Body request: UpdatePaymentMethodRequest) : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/updateStoreDomain")
    suspend fun updateStoreDomain(@Body request: StoreLinkRequest) : Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getAccountInfo")
    suspend fun getProfileResponse() : Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getProfileInfo")
    suspend fun getProfilePreviewResponse() : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setServices")
    suspend fun changeStoreAndDeliveryStatus(@Body request: StoreDeliveryStatusChangeRequest) : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setStoreDescription")
    suspend fun setStoreDescription(@Body request: StoreDescriptionRequest) : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setStoreName")
    suspend fun setStoreName(@Body request: StoreNameRequest) : Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getAllBusinessList")
    suspend fun getBusinessList() : Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getReferPageInfo")
    suspend fun getReferAndEarnData() : Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getStoreControlPageInfo")
    suspend fun getStoreControlPageInfo() : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setStoreBusinesses")
    suspend fun setStoreBusinesses(@Body request: BusinessTypeRequest) : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setDeliveryInfo")
    suspend fun updateDeliveryInfo(@Body request: MoreControlsRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setBankDetails")
    suspend fun setBankDetails(@Body request: BankDetailsRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/initiateKyc")
    suspend fun initiateKyc(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getBankDetailsPageInfo")
    suspend fun getBankDetailsPageInfo(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getPaymentModesPageInfo")
    suspend fun getPaymentModesPageInfo(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getOrderNotificationPageInfo")
    suspend fun getOrderNotificationPageInfo(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getStoreUserPageInfo")
    suspend fun getStoreUserPageInfo(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setStoreUserInfo")
    suspend fun setStoreUserInfo(@Body request: StoreUserMailDetailsRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setPaymentOptions")
    suspend fun setPaymentOptions(@Body request: PaymentModeRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/updateNotificationFlag")
    suspend fun updateNotificationFlag(@Body request: UpdatePaymentMethodRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getAddressFieldsPageInfo")
    suspend fun getAddressFieldsPageInfo(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getPosBillingPageInfo")
    suspend fun getPosBillingPageInfo(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setStoreAddressConfigs")
    suspend fun setAddressFields(@Body request: AddressFieldRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getReferralData")
    suspend fun getReferralData(): Response<CommonApiResponse>

    /* ----------------------       Images         ---------------------- */
    @POST("api/dotk/vm1/media/setStoreLogo")
    suspend fun setStoreLogo(@Body request: StoreLogoRequest) : Response<CommonApiResponse>

    @Multipart
    @POST("api/dotk/vm1/media/uploadMediaToS3")
    suspend fun getImageUploadCdnLink(@Part("media_type") imageType: RequestBody, @Part file: MultipartBody.Part?): Response<CommonApiResponse>

    @GET("api/dotk/merchant/searchImages")
    suspend fun searchImagesFromBing(@Query("search_text") searchText: String, @Query("store_id") storeId: String) : Response<ImagesSearchResponse>

    /* ----------------------       Marketing         ---------------------- */

    @GET("api/dotk/vm1/marketing/getTemplatesBackground/{id}")
    suspend fun getSocialMediaTemplateBackgrounds(@Path("id") id: String): Response<CommonApiResponse>

    @GET("api/dotk/vm1/marketing/getSuggestedDomainsInfo")
    suspend fun getMarketingSuggestedDomains(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/marketing/getMarketingPageInfo")
    suspend fun getStoreMarketingPageInfo(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/marketing/getSocialMediaPageInfo")
    suspend fun getSocialMediaPageInfo(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/marketing/getSocialMediaTemplateList/{id}")
    suspend fun getSocialMediaTemplateList(@Path("id") id: String, @Query("page") page: Int): Response<CommonApiResponse>

    @POST("api/dotk/vm1/marketing/shareStore")
    suspend fun getShareStore(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/marketing/saveSocialMediaPost")
    suspend fun saveSocialMediaPost(@Body request: SaveSocialMediaPostRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/marketing/setSocialMediaFavourite")
    suspend fun setSocialMediaFavourite(@Body request: SocialMediaTemplateFavouriteRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/marketing/shareStorePdfText")
    suspend fun getShareStorePdfText(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/marketing/generateStorePdf")
    suspend fun generateStorePdf(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/marketing/generateStorePdf")
    suspend fun generateProductStorePdf(): Response<CommonApiResponse>

    /* ----------------------       Products         ---------------------- */

    @GET("api/dotk/vm1/products/getItemsBasicDetailsByStoreId")
    suspend fun getItemsBasicDetailsByStoreId(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getItemsBasicDetails/{id}")
    suspend fun getProductsByCategoryId(@Path("id") id: Int): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getUserCategories")
    suspend fun getProductsCategories(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/shareStore")
    suspend fun getProductShareStoreData(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getProductPageInfo")
    suspend fun getProductPageInfo(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getMasterCatalogStaticText")
    suspend fun getMasterCatalogStaticText(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getItemInfo/{itemId}")
    suspend fun getItemInfo(@Path("itemId") itemId: Int): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/quickUpdateItem")
    suspend fun quickUpdateItemInventory(@Body request: UpdateItemInventoryRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/setItemV2")
    suspend fun setItem(@Body request: AddProductRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/searchItems")
    suspend fun searchItems(@Body request: SearchCatalogItemsRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getAllMasterCategoriesList")
    suspend fun getMasterCategories(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getMasterSubCategories/{id}")
    suspend fun getMasterSubCategories(@Path("id") id: Int): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getMasterItems/{id}")
    suspend fun getMasterItems(@Path("id") id: Int, @Query("page") page: Int): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/buildCatalog")
    suspend fun buildCatalog(@Body request: BuildCatalogRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/updateStock")
    suspend fun updateStock(@Body request: UpdateStockRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getDeleteCategoryInfo")
    suspend fun getDeleteCategoryInfo(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/updateCategory")
    suspend fun updateCategory(@Body request: UpdateCategoryRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/deleteCategory")
    suspend fun deleteCategory(@Body request: DeleteCategoryRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/deleteItem")
    suspend fun deleteItem(@Body request: DeleteItemRequest): Response<CommonApiResponse>

    /* ----------------------       Orders         ---------------------- */
    @GET("api/dotk/vm1/orders/getLandingPageCards")
    suspend fun getLandingPageCards() : Response<CommonApiResponse>

    @GET("api/dotk/vm1/orders/getOrderAnalytics")
    suspend fun getAnalyticsData() : Response<CommonApiResponse>

    @GET("api/dotk/vm1/orders/sharePaymentLink/{order_id}")
    suspend fun sharePaymentLink(@Path("order_id") orderId: String?): Response<CommonApiResponse>

    @GET("api/dotk/vm1/orders/getOrdersPageInfo")
    suspend fun getOrderPageInfo() : Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/sendPaymentLink")
    suspend fun sendPaymentLink(@Body request: PaymentLinkRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/getOrderList")
    suspend fun getOrdersList(@Body request: OrdersRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/getSearchOrdersList")
    suspend fun getSearchOrdersList(@Body request: SearchOrdersRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/updateOrderStatus")
    suspend fun updateOrderStatus(@Body statusRequest: UpdateOrderStatusRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/orders/getOrderDetailsV2/{orderId}")
    suspend fun getOrderDetails(@Path("orderId") orderId: String): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/completeOrder")
    suspend fun completeOrder(@Body request: CompleteOrderRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/orders/getDeliveryTime")
    suspend fun getDeliveryTime(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/orders/getCartFilterOptions")
    suspend fun getCartFilterOptions(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/onboard/getOnboardingPageInfo")
    suspend fun getCustomDomainBottomSheetData(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/updateOrderV2")
    suspend fun updateOrder(@Body statusRequest: UpdateOrderRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/updatePrepaidOrder/{orderId}")
    suspend fun updatePrepaidOrder(@Path("orderId") orderId: String?, @Body statusRequest: UpdatePrepaidOrderRequest?): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/shareBill/{order_id}")
    suspend fun shareBill(@Path("order_id") orderId: String): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/getCartsByFilters")
    suspend fun getCartsByFilters(): Response<CommonApiResponse>

    /* ----------------------       Payments         ---------------------- */
    @GET("api/dotk/vm1/settings/getOrderBannerDetails/{orderId}")
    suspend fun getOrderTransactions(@Path("orderId") orderId: String?): Response<CommonApiResponse>


    /* ----------------------       Premium         ---------------------- */
    @GET("api/dotk/vm1/premium/getDomainSuggestionList/{count}")
    suspend fun getDomainSuggestionList(@Path("count") count:Int): Response<CommonApiResponse>

    @GET("api/dotk/vm1/premium/getPremiumPageInfo")
    suspend fun getPremiumPageInfo(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/premium/getAllPresetColors")
    suspend fun getAllPresetColors(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/premium/setStoreThemeColorPalette")
    suspend fun setStoreThemeColorPalette(@Body request: EditPremiumColorRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/premium/addRequestToCallback")
    suspend fun updateCallbackFlag(@Body request: RequestToCallbackRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/premium/setStoreThemeBanner")
    suspend fun setStoreThemeBanner(@Body request: StoreThemeBannerRequest): Response<CommonApiResponse>

    /* ----------------------       Coupons & Promos         ---------------------- */
    @POST("api/dotk/vm1/promo/createPromoCode")
    suspend fun createPromoCode(@Body request: CreateCouponsRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/promo/updatePromoCodeStatus")
    suspend fun updatePromoCodeStatus(@Body request: UpdatePromoCodeRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/promo/getAllMerchantPromoCodes")
    suspend fun getAllMerchantPromoCodes(@Body request: GetPromoCodeRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/promo/getPromoCodePageInfo")
    suspend fun getPromoCodePageInfo(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/promo/getCouponDetails")
    suspend fun getCouponDetails(@Query("promo_code") promoCode: String) : Response<CommonApiResponse>

    @GET("api/dotk/vm1/promo/shareCoupon")
    suspend fun shareCoupon(@Query("promo_code") promoCode: String) : Response<CommonApiResponse>

    /* ----------------------       Others         ---------------------- */
    @POST("api/dotanalytics/push/androidEventLog")
    suspend fun androidEventLog(@Body request: AndroidEventLogRequest): Response<CommonApiResponse>

    @GET("api/dotk/template/referAppWa")
    suspend fun getReferAndEarnDataOverWhatsApp() : Response<ReferEarnOverWhatsAppResponse>

    @GET("api/dotk/vm1/settings/getLockedStoreShareData/{mode}")
    suspend fun getLockedStoreShareData(@Path("mode") mode: Int) : Response<CommonApiResponse>
}