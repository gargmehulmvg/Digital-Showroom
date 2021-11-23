package com.digitaldukaan.fragments

import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.appsflyer.AppsFlyerLib
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.CustomDomainSelectionAdapter
import com.digitaldukaan.adapters.LandingPageCardsAdapter
import com.digitaldukaan.adapters.LandingPageShortcutsAdapter
import com.digitaldukaan.adapters.OrderAdapterV2
import com.digitaldukaan.constants.*
import com.digitaldukaan.constants.StaticInstances.sIsInvitationShown
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.ILandingPageAdapterListener
import com.digitaldukaan.interfaces.IOrderListItemListener
import com.digitaldukaan.models.dto.CleverTapProfile
import com.digitaldukaan.models.request.CompleteOrderRequest
import com.digitaldukaan.models.request.OrdersRequest
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.OrderFragmentService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.layout_analytics.*
import kotlinx.android.synthetic.main.layout_home_fragment.*
import kotlinx.android.synthetic.main.layout_home_fragment.analyticsContainer
import kotlinx.android.synthetic.main.layout_home_fragment.analyticsImageView
import kotlinx.android.synthetic.main.layout_home_fragment.orderLayout
import kotlinx.android.synthetic.main.layout_order_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OrderFragment : BaseFragment(), IHomeServiceInterface, PopupMenu.OnMenuItemClickListener,
    SwipeRefreshLayout.OnRefreshListener, IOrderListItemListener {

    private var ePosTextView: TextView? = null
    private var searchImageView: ImageView? = null
    private var toolbarSearchImageView: ImageView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var mIsNewUserLogin = false
    private var mIsDoublePressToExit = false
    private var subHeadingTextView: TextView? = null
    private var domainExpiryTextView: TextView? = null
    private var ordersRecyclerView: RecyclerView? = null
    private var completedOrdersRecyclerView: RecyclerView? = null
    private var mOrderAdapter: OrderAdapterV2? = null
    private var mCompletedOrderAdapter: OrderAdapterV2? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mPendingPageCount = 1
    private var mCompletedPageCount = 1
    private var mLandingPageAdapterPosition = 1
    private var mLandingPageAdapterItem: ZeroOrderItemsResponse? = null
    private var mService: OrderFragmentService? = null
    private var mLandingPageAdapter: LandingPageCardsAdapter? = null
    private var mIsAllStepsCompleted: Boolean = false
    private var mIsToolbarSearchAvailable: Boolean = false
    private var mPaymentLinkBottomSheet: BottomSheetDialog? = null
    private var mPaymentLinkAmountStr: String? = null
    private var mStaffInvitation: StaffInvitationResponse? = null
    private var mIsInvitationShown: Boolean = false
    private var mUserId: String = ""

    companion object {
        private var sOrderPageInfoResponse: OrderPageInfoResponse? = null
        private var sOrderPageInfoStaticData: OrderPageStaticTextResponse? = null
        private var sAnalyticsResponse: AnalyticsResponse? = null
        private var sDoubleClickToExitStr: String? = ""
        private var sFetchingOrdersStr: String? = ""
        private var sMobileNumberString = ""
        private var sOrderIdString = ""
        private var sIsMorePendingOrderAvailable = false
        private var sIsMoreCompletedOrderAvailable = false
        private var sOrderList: ArrayList<OrderItemResponse> = ArrayList()
        private var sCompletedOrderList: ArrayList<OrderItemResponse> = ArrayList()
        private var sCheckStaffInviteResponse: StaffMemberDetailsResponse? = null

        fun newInstance(isNewUserLogin: Boolean = false): OrderFragment {
            val fragment = OrderFragment()
            fragment.mIsNewUserLogin = isNewUserLogin
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        TAG = "OrderFragment"
        super.onCreate(savedInstanceState)
        AppsFlyerLib.getInstance().setCustomerUserId(PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER))
        Log.d(TAG, "onCreate: FIREBASE ANALYTICS CALLED")
        val firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.setUserId(PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER))
        Log.d(TAG, "onCreate: FIREBASE ANALYTICS END")
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("store_id", PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
            setCustomKey("store_name", PrefsManager.getStringDataFromSharedPref(Constants.STORE_NAME))
            setCustomKey("mobile_number", PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "OrderFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_order_fragment, container, false)
        mService = OrderFragmentService()
        mService?.setServiceListener(this)
        initializeViews()
        mService?.checkStaffInvite()
        if (!askContactPermission()) {
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
            } else {
                if (null == sOrderPageInfoResponse) {
                    mService?.getOrderPageInfo()
                    mService?.getAnalyticsData()
                } else {
                    setupOrderPageInfoUI()
                    setupAnalyticsUI()
                }
            }
        }
        if (mIsNewUserLogin) {
            mIsNewUserLogin = false
            if (null == StaticInstances.sCustomDomainBottomSheetResponse)
                mService?.getCustomDomainBottomSheetData()
            else
                StaticInstances.sCustomDomainBottomSheetResponse?.let { response -> showCustomDomainBottomSheet(response) }
        }
        return mContentView
    }

    private fun initializeViews() {
        ePosTextView = mContentView?.findViewById(R.id.ePosTextView)
        searchImageView = mContentView?.findViewById(R.id.searchImageView)
        toolbarSearchImageView = mContentView?.findViewById(R.id.toolbarSearchImageView)
        swipeRefreshLayout = mContentView?.findViewById(R.id.swipeRefreshLayout)
        subHeadingTextView = mContentView?.findViewById(R.id.ordersSubHeadingTextView)
        domainExpiryTextView = mContentView?.findViewById(R.id.domainExpiryTextView)
        ordersRecyclerView = mContentView?.findViewById(R.id.ordersRecyclerView)
        completedOrdersRecyclerView = mContentView?.findViewById(R.id.completedOrdersRecyclerView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateNavigationBarState(R.id.menuHome)
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        WebViewBridge.mWebViewListener = this
        hideBottomNavigationView(false)
        swipeRefreshLayout?.setOnRefreshListener(this)
        mPendingPageCount = 1
        mCompletedPageCount = 1
        sOrderList.clear()
        sCompletedOrderList.clear()
        ePosTextView?.visibility = View.GONE
        orderLayout?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                Log.d(TAG, "Scroll DOWN")
                ePosTextView?.visibility = View.GONE
                if (mIsToolbarSearchAvailable) toolbarSearchImageView?.visibility = View.VISIBLE
            }
            if (scrollY < oldScrollY) {
                Log.d(TAG, "Scroll UP")
                ePosTextView?.visibility = View.VISIBLE
                ePosTextView?.text = sOrderPageInfoStaticData?.text_epos
            }
            if (0 == scrollY) {
                Log.d(TAG, "Scroll UP Most")
                ePosTextView?.visibility = View.VISIBLE
                toolbarSearchImageView?.visibility = View.GONE
                ePosTextView?.text = sOrderPageInfoStaticData?.text_epos
            }
            if (scrollY == (v.getChildAt(0).measuredHeight - v.measuredHeight)) {
                Log.d(TAG, "Scroll To Bottom Last")
                when {
                    sIsMorePendingOrderAvailable -> {
                        ++mPendingPageCount
                        fetchLatestOrders(Constants.MODE_PENDING, "", mPendingPageCount)
                    }
                    sIsMoreCompletedOrderAvailable -> {
                        ++mCompletedPageCount
                        fetchLatestOrders(Constants.MODE_COMPLETED, "", mCompletedPageCount)
                    }
                    !sIsMoreCompletedOrderAvailable -> {
                        if (isEmpty(sCompletedOrderList)) {
                            mCompletedPageCount = 1
                            fetchLatestOrders(Constants.MODE_COMPLETED, "", mCompletedPageCount)
                        }
                    }
                }
            }
        })
        ordersRecyclerView?.apply {
            isNestedScrollingEnabled = false
            mActivity?.let { context -> mOrderAdapter = OrderAdapterV2(context, sOrderList) }
            mOrderAdapter?.setCheckBoxListener(this@OrderFragment)
            mLinearLayoutManager = LinearLayoutManager(mActivity)
            layoutManager = mLinearLayoutManager
            adapter = mOrderAdapter
            addItemDecoration(StickyRecyclerHeadersDecoration(mOrderAdapter))
        }
        completedOrdersRecyclerView?.apply {
            isNestedScrollingEnabled = false
            mActivity?.let { context -> mCompletedOrderAdapter = OrderAdapterV2(context, sCompletedOrderList) }
            mCompletedOrderAdapter?.setCheckBoxListener(this@OrderFragment)
            mLinearLayoutManager = LinearLayoutManager(mActivity)
            layoutManager = mLinearLayoutManager
            adapter = mCompletedOrderAdapter
            addItemDecoration(StickyRecyclerHeadersDecoration(mCompletedOrderAdapter))
        }
    }

    override fun onUserAuthenticationResponse(authenticationUserResponse: ValidateOtpResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            saveUserDetailsInPref(authenticationUserResponse)
            if (!authenticationUserResponse.mIsSuccessStatus) showShortSnackBar(authenticationUserResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    private fun saveUserDetailsInPref(validateOtpResponse: ValidateOtpResponse) {
        storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, validateOtpResponse.mUserAuthToken)
        storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, validateOtpResponse.mUserPhoneNumber)
        validateOtpResponse.mStore?.let { store ->
            storeStringDataInSharedPref(Constants.STORE_ID, store.storeId.toString())
            storeStringDataInSharedPref(Constants.STORE_NAME, store.storeInfo.name)
            if (isNotEmpty(store.storeInfo.logoImage)) StaticInstances.sIsStoreImageUploaded = true
        }
    }

    override fun onPendingOrdersResponse(getOrderResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (true == swipeRefreshLayout?.isRefreshing) swipeRefreshLayout?.isRefreshing = false
            if (getOrderResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(getOrderResponse.mCommonDataStr, OrdersResponse::class.java)
                sIsMorePendingOrderAvailable = ordersResponse.mIsNextDataAvailable
                if (1 == mPendingPageCount) sOrderList.clear()
                val pendingOrderTextView: TextView? = mContentView?.findViewById(R.id.pendingOrderTextView)
                if (isNotEmpty(ordersResponse.mOrdersList)) {
                    pendingOrderTextView?.visibility = View.VISIBLE
                    mIsToolbarSearchAvailable = true
                    sOrderList.addAll(ordersResponse.mOrdersList)
                    if (!sIsMorePendingOrderAvailable) fetchLatestOrders(Constants.MODE_COMPLETED, "", 1)
                } else {
                    pendingOrderTextView?.visibility = View.GONE
                    mIsToolbarSearchAvailable = false
                    sOrderList.addAll(ArrayList())
                    sCompletedOrderList.clear()
                    fetchLatestOrders(Constants.MODE_COMPLETED, "", 1)
                }
                convertDateStringOfOrders(sOrderList)
                mOrderAdapter?.setOrderList(sOrderList)
            }
        }
    }

    override fun onCompletedOrdersResponse(getOrderResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (swipeRefreshLayout?.isRefreshing == true) swipeRefreshLayout?.isRefreshing = false
            if (getOrderResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(getOrderResponse.mCommonDataStr, OrdersResponse::class.java)
                sIsMoreCompletedOrderAvailable = ordersResponse.mIsNextDataAvailable
                val completedOrderTextView: TextView? = mContentView?.findViewById(R.id.completedOrderTextView)
                if (mCompletedPageCount == 1) sCompletedOrderList.clear()
                if (isNotEmpty(ordersResponse.mOrdersList)) {
                    completedOrderTextView?.visibility = View.VISIBLE
                    mIsToolbarSearchAvailable = true
                    sCompletedOrderList.addAll(ordersResponse.mOrdersList)
                } else {
                    completedOrderTextView?.visibility = View.GONE
                    sCompletedOrderList.addAll(ArrayList())
                }
                convertDateStringOfOrders(sCompletedOrderList)
                mCompletedOrderAdapter?.setOrderList(sCompletedOrderList)
            }
        }
    }

    override fun onAnalyticsDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                sAnalyticsResponse = Gson().fromJson<AnalyticsResponse>(commonResponse.mCommonDataStr, AnalyticsResponse::class.java)
                setupAnalyticsUI()
            }
        }
    }

    override fun onOrderPageInfoResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                sOrderPageInfoResponse = Gson().fromJson<OrderPageInfoResponse>(commonResponse.mCommonDataStr, OrderPageInfoResponse::class.java)
                showDialogOrNot()
                pushProfileToCleverTap()
            }
        }
    }

    override fun onSearchOrdersResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (true == swipeRefreshLayout?.isRefreshing) swipeRefreshLayout?.isRefreshing = false
            if (commonResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(commonResponse.mCommonDataStr, OrdersResponse::class.java)
                if (isNotEmpty(ordersResponse?.mOrdersList))
                    launchFragment(SearchOrdersFragment.newInstance(
                        sOrderIdString,
                        sMobileNumberString, ordersResponse.mOrdersList), true) else {
                        showSearchDialog(StaticInstances.sOrderPageInfoStaticData, sMobileNumberString, sOrderIdString, true)
                }
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onOrdersUpdatedStatusResponse(commonResponse: CommonApiResponse) {
        Log.d(TAG, "onOrdersUpdatedStatusResponse: do nothing :: $commonResponse")
    }

    override fun onCompleteOrderStatusResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (swipeRefreshLayout?.isRefreshing == true) swipeRefreshLayout?.isRefreshing = false
            if (commonResponse.mIsSuccessStatus) {
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_check_circle)
                orderLayout?.fullScroll(ScrollView.FOCUS_UP)
                mCompletedPageCount = 1
                mPendingPageCount = 1
                sOrderList.clear()
                sCompletedOrderList.clear()
                fetchLatestOrders(Constants.MODE_PENDING,
                    sFetchingOrdersStr,
                    mPendingPageCount
                )
                if (!PrefsManager.getBoolDataFromSharedPref(PrefsManager.KEY_FIRST_ITEM_COMPLETED)) {
                    PrefsManager.storeBoolDataInSharedPref(PrefsManager.KEY_FIRST_ITEM_COMPLETED, true)
                    mActivity?.launchInAppReviewDialog()
                }
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onCustomDomainBottomSheetDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                val customDomainBottomSheetResponse = Gson().fromJson<CustomDomainBottomSheetResponse>(commonResponse.mCommonDataStr, CustomDomainBottomSheetResponse::class.java)
                showCustomDomainBottomSheet(customDomainBottomSheetResponse)
            }
        }
    }

    override fun onLandingPageCardsResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                val landingPageCardsResponse = Gson().fromJson<LandingPageCardsResponse>(commonResponse.mCommonDataStr, LandingPageCardsResponse::class.java)
                Log.d(TAG, "onLandingPageCardsResponse: $landingPageCardsResponse")
                mIsAllStepsCompleted = false
                StaticInstances.sIsShareStoreLocked = landingPageCardsResponse?.isShareStoreLocked ?: false
                landingPageCardsResponse?.zeroOrderItemsList?.forEachIndexed { _, itemsResponse ->
                    mIsAllStepsCompleted = itemsResponse?.completed?.isCompleted ?: false
                }
                val nextStepTextView: TextView? = mContentView?.findViewById(R.id.nextStepTextView)
                val ordersSubHeadingTextView: View? = mContentView?.findViewById(R.id.ordersSubHeadingTextView)
                val domainExpiryContainer: View? = mContentView?.findViewById(R.id.domainExpiryContainer)
                if (mIsAllStepsCompleted) {
                    zeroOrderItemsRecyclerView?.visibility = View.GONE
                    if(StaticInstances.sPermissionHashMap?.get("my_shortcuts") == true){
                        myShortcutsRecyclerView?.visibility = View.VISIBLE
                        myShortcutsRecyclerView?.apply {
                            layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                            adapter = LandingPageShortcutsAdapter(mActivity, landingPageCardsResponse?.shortcutsList, object : IAdapterItemClickListener {
                                override fun onAdapterItemClickListener(position: Int) {
                                    val item = landingPageCardsResponse?.shortcutsList?.get(position)
                                    when(item?.action) {
                                        Constants.ACTION_ADD_PRODUCT -> launchFragment(AddProductFragment.newInstance(0, true), true)
                                        Constants.ACTION_SHARE_STORE -> {
                                            if (StaticInstances.sIsShareStoreLocked) {
                                                getLockedStoreShareDataServerCall(Constants.MODE_SHARE_STORE)
                                                return
                                            }
                                            shareStoreOverWhatsAppServerCall()
                                        }
                                        Constants.ACTION_MY_PROFILE -> launchFragment(ProfilePreviewFragment.newInstance(), true)
                                        Constants.ACTION_STORE_CONTROLS -> initiateAccountInfoServerCall()
                                    }
                                }

                                private fun initiateAccountInfoServerCall() {
                                    showProgressDialog(mActivity)
                                    CoroutineScopeUtils().runTaskOnCoroutineBackground {
                                        try {
                                            val response = RetrofitApi().getServerCallObject()?.getProfileResponse()
                                            response?.let {
                                                stopProgress()
                                                if (it.isSuccessful) {
                                                    it.body()?.let {
                                                        withContext(Dispatchers.Main) {
                                                            stopProgress()
                                                            if (it.mIsSuccessStatus) {
                                                                val accountInfoResponse = Gson().fromJson<AccountInfoResponse>(it.mCommonDataStr, AccountInfoResponse::class.java)
                                                                StaticInstances.sAccountPageSettingsStaticData = accountInfoResponse?.mAccountStaticText
                                                                StaticInstances.sAppStoreServicesResponse = accountInfoResponse?.mStoreInfo?.storeServices
                                                                launchFragment(MoreControlsFragment.newInstance(accountInfoResponse), true)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            exceptionHandlingForAPIResponse(e)
                                        }
                                    }
                                }
                            })
                        }
                    }
                } else {
                    zeroOrderItemsRecyclerView?.visibility = View.VISIBLE
                    myShortcutsRecyclerView?.visibility = View.GONE
                    zeroOrderItemsRecyclerView?.apply {
                        layoutManager = LinearLayoutManager(mActivity)
                        mLandingPageAdapter = getLandingPageCardsAdapter(landingPageCardsResponse)
                        adapter = mLandingPageAdapter
                    }
                }
                nextStepTextView?.text = if (mIsAllStepsCompleted) sOrderPageInfoStaticData?.text_my_shortcuts else sOrderPageInfoStaticData?.text_next_steps
                ordersSubHeadingTextView?.visibility = if (mIsAllStepsCompleted) View.GONE else View.VISIBLE
                domainExpiryContainer?.visibility = if (isEmpty(landingPageCardsResponse?.domainExpiryMessage)) View.GONE else {
                    domainExpiryContainer?.visibility = View.VISIBLE
                    if (isNotEmpty(landingPageCardsResponse.domainExpiryMessage))
                        Handler(Looper.getMainLooper()).postDelayed({
                            domainExpiryContainer?.visibility = View.VISIBLE
                            Handler(Looper.getMainLooper()).postDelayed({
                                domainExpiryTextView?.apply {
                                    visibility = View.VISIBLE
                                    text = landingPageCardsResponse.domainExpiryMessage
                                }
                            }, Constants.AUTO_DISMISS_PROGRESS_DIALOG_TIMER)
                        }, Constants.AUTO_DISMISS_PROGRESS_DIALOG_TIMER)
                    View.VISIBLE
                }
            }
        }
    }

    private fun getLandingPageCardsAdapter(landingPageCardsResponse: LandingPageCardsResponse?) =
        LandingPageCardsAdapter(landingPageCardsResponse?.zeroOrderItemsList, this, object : ILandingPageAdapterListener {

                override fun onLandingPageAdapterIsPrimaryDetected(position: Int, item: ZeroOrderItemsResponse?) {
                    mLandingPageAdapterPosition = position
                    mLandingPageAdapterItem = item
                    when (item?.id) {
                        Constants.KEY_BUY_DOMAIN -> {
                            if (isEmpty(StaticInstances.sSuggestedDomainsList))
                                mService?.getDomainSuggestionList(mActivity?.resources?.getInteger(R.integer.custom_domain_count) ?: 4)
                        }
                    }
                }

            override fun onLandingPageAdapterCustomDomainApplyItemClicked(item: PrimaryDomainItemResponse?) {
                if (Constants.NEW_RELEASE_TYPE_WEBVIEW == item?.cta?.action) {
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_DOMAIN_APPLY,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                            AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.LANDING_PAGE)
                    )
                    val url = "${item.cta?.pageUrl}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&domain_name=${item.domainName}&purchase_price=${item.originalPrice}&renewal_price=${item.renewalPrice}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.LANDING_PAGE}"
                    openWebViewFragmentV3(this@OrderFragment, "", url)
                }
            }

            override fun onLandingPageAdapterAddProductItemClicked() = launchFragment(AddProductFragment.newInstance(0, true), true)

            override fun onLandingPageAdapterCtaClicked(item: ZeroOrderItemsResponse?) {
                Log.d(TAG, "onLandingPageAdapterCtaClicked: ZeroOrderItemsResponse item :: $item")
                when(item?.ctaResponse?.action) {
                    Constants.NEW_RELEASE_TYPE_WEBVIEW -> {
                        if (isNotEmpty(item.ctaResponse?.url)) {
                            val eventName = when(item.id) {
                                Constants.KEY_BUY_DOMAIN -> AFInAppEventType.EVENT_DOMAIN_EXPLORE
                                Constants.KEY_ADD_PRODUCT -> AFInAppEventType.EVENT_ADD_ITEM
                                else -> AFInAppEventType.EVENT_GET_PREMIUM_WEBSITE
                            }
                            AppEventsManager.pushAppEvents(
                                eventName = eventName,
                                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                    AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.LANDING_PAGE)
                            )
                            val url = "${item.ctaResponse?.url}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.LANDING_PAGE}"
                            openWebViewFragmentV3(this@OrderFragment, "", url)
                        }
                    }
                    else -> {
                        launchFragment(AddProductFragment.newInstance(0, true), true)
                    }
                }
            }

            override fun onLandingPageCustomCtaClicked(url: String?) = openWebViewFragmentV3(this@OrderFragment, "", url)
        })

    override fun onDomainSuggestionListResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                val listType = object : TypeToken<ArrayList<PrimaryDomainItemResponse>>() {}.type
                val customDomainList = Gson().fromJson<ArrayList<PrimaryDomainItemResponse>>(commonResponse.mCommonDataStr, listType)
                Log.d(TAG, "onDomainSuggestionListResponse: list :: $customDomainList")
                mLandingPageAdapterItem?.suggestedDomainsList = customDomainList
                StaticInstances.sSuggestedDomainsList = ArrayList()
                StaticInstances.sSuggestedDomainsList = mLandingPageAdapterItem?.suggestedDomainsList
                mLandingPageAdapter?.setItemToPosition(mLandingPageAdapterPosition, mLandingPageAdapterItem)
            }
        }
    }

    override fun onHomePageException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onClick(view: View?) {
        when (view?.id) {
            analyticsImageView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_ORDER_ANALYTICS,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                analyticsContainer?.visibility = View.VISIBLE
                mActivity?.let { context -> analyticsImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_analytics_green)) }
            }
            closeAnalyticsImageView?.id -> {
                mActivity?.let { context -> analyticsImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_analytics_black)) }
                analyticsContainer?.visibility = View.GONE
            }
            shareStoreTextView?.id -> {
                if (StaticInstances.sIsShareStoreLocked) {
                    getLockedStoreShareDataServerCall(Constants.MODE_SHARE_STORE)
                } else shareStoreOverWhatsAppServerCall()
            }
            searchImageView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SEARCH_INTENT,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                showSearchDialog(sOrderPageInfoStaticData, sMobileNumberString, sOrderIdString)
            }
            toolbarSearchImageView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SEARCH_INTENT,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                showSearchDialog(sOrderPageInfoStaticData, sMobileNumberString, sOrderIdString)
            }
            ePosContainer?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_TAKE_ORDER,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                showEPosBottomSheet()
            }
        }
    }

    private fun setupAnalyticsUI() {
        try {
            val todaySaleValue: TextView? = mContentView?.findViewById(R.id.todaySaleValue)
            val amountValue: TextView? = mContentView?.findViewById(R.id.amountValue)
            val weekSaleValue: TextView? = mContentView?.findViewById(R.id.weekSaleValue)
            val weekAmountValue: TextView? = mContentView?.findViewById(R.id.weekAmountValue)
            val todaySaleHeading: TextView? = mContentView?.findViewById(R.id.todaySaleHeading)
            val amountHeading: TextView? = mContentView?.findViewById(R.id.amountHeading)
            val weekSaleHeading: TextView? = mContentView?.findViewById(R.id.weekSaleHeading)
            val weekAmountHeading: TextView? = mContentView?.findViewById(R.id.weekAmountHeading)
            todaySaleValue?.text = sAnalyticsResponse?.today?.totalCount.toString()
            val amountValueStr = "${sAnalyticsResponse?.analyticsStaticData?.textRuppeeSymbol} ${sAnalyticsResponse?.today?.totalAmount}"
            amountValue?.text = amountValueStr
            weekSaleValue?.text = sAnalyticsResponse?.thisWeek?.totalCount.toString()
            val weekValueStr = "${sAnalyticsResponse?.analyticsStaticData?.textRuppeeSymbol} ${sAnalyticsResponse?.thisWeek?.totalAmount}"
            weekAmountValue?.text = weekValueStr
            sAnalyticsResponse?.analyticsStaticData?.run {
                todaySaleHeading?.text = textTodaySale
                amountHeading?.text = textTodayAmount
                weekSaleHeading?.text = textWeekSale
                weekAmountHeading?.text = textWeekAmount
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupAnalyticsUI: ${e.message}", e)
        }
    }

    private fun showCustomDomainBottomSheet(customDomainBottomSheetResponse: CustomDomainBottomSheetResponse) {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_custom_domain_selection, it.findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                view?.run {
                    customDomainBottomSheetResponse.staticText?.let { staticText ->
                        val searchTextView: TextView = findViewById(R.id.searchTextView)
                        val headingTextView: TextView = findViewById(R.id.headingTextView)
                        val subHeadingTextView: TextView = findViewById(R.id.subHeadingTextView)
                        val searchMessageTextView: TextView = findViewById(R.id.searchMessageTextView)
                        val moreSuggestionsTextView: TextView = findViewById(R.id.moreSuggestionsTextView)
                        subHeadingTextView.text = staticText.subheading_budiness_needs_domain
                        headingTextView.text = staticText.heading_last_step
                        moreSuggestionsTextView.text = staticText.text_more_suggestions
                        searchMessageTextView.text = staticText.text_cant_find
                        searchTextView.text = staticText.text_search
                        searchTextView.setOnClickListener {
                            bottomSheetDialog.dismiss()
                            if (Constants.NEW_RELEASE_TYPE_WEBVIEW == customDomainBottomSheetResponse.searchCta?.action) {
                                val url = BuildConfig.WEB_VIEW_URL + "${customDomainBottomSheetResponse.searchCta?.pageUrl}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.ON_BOARDING}"
                                openWebViewFragmentV3(this@OrderFragment, "", url)
                            }
                        }
                    }
                    customDomainBottomSheetResponse.primaryDomain?.let { primaryDomain ->
                        val premiumHeadingTextView: TextView = findViewById(R.id.premiumHeadingTextView)
                        val domainTextView: TextView = findViewById(R.id.domainTextView)
                        val priceTextView: TextView = findViewById(R.id.priceTextView)
                        val promoCodeTextView: TextView = findViewById(R.id.promoCodeTextView)
                        val messageTextView: TextView = findViewById(R.id.messageTextView)
                        val message2TextView: TextView = findViewById(R.id.message2TextView)
                        val originalPriceTextView: TextView = findViewById(R.id.originalPriceTextView)
                        val buyNowTextView: TextView = findViewById(R.id.buyNowTextView)
                        premiumHeadingTextView.text = primaryDomain.heading
                        domainTextView.text = primaryDomain.domainName
                        promoCodeTextView.text = primaryDomain.promo
                        var amount = "₹${primaryDomain.discountedPrice}"
                        priceTextView.text = amount
                        amount = "₹${primaryDomain.originalPrice}"
                        originalPriceTextView.text = amount
                        originalPriceTextView.showStrikeOffText()
                        buyNowTextView.apply {
                            text = primaryDomain.cta?.text
                            setTextColor(Color.parseColor(primaryDomain.cta?.textColor))
                            setBackgroundColor(Color.parseColor(primaryDomain.cta?.textBg))
                            setOnClickListener {
                                bottomSheetDialog.dismiss()
                                if (Constants.NEW_RELEASE_TYPE_WEBVIEW == primaryDomain.cta?.action) {
                                    val url = BuildConfig.WEB_VIEW_URL + "${primaryDomain.cta?.pageUrl}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&domain_name=${primaryDomain.domainName}&purchase_price=${primaryDomain.originalPrice}&renewal_price=${primaryDomain.renewalPrice}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.ON_BOARDING}"
                                    openWebViewFragmentV3(this@OrderFragment, "", url)
                                }
                            }
                        }
                        messageTextView.text = primaryDomain.infoData?.firstYearText?.trim()
                        message2TextView.text = primaryDomain.infoData?.renewsText?.trim()
                    }
                    val suggestedDomainRecyclerView = findViewById<RecyclerView>(R.id.suggestedDomainRecyclerView)
                    suggestedDomainRecyclerView.apply {
                        layoutManager = LinearLayoutManager(mActivity)
                        adapter = CustomDomainSelectionAdapter(
                            customDomainBottomSheetResponse.suggestedDomainsList,
                            object : IAdapterItemClickListener {

                                override fun onAdapterItemClickListener(position: Int) {
                                    bottomSheetDialog.dismiss()
                                    val item = customDomainBottomSheetResponse.suggestedDomainsList?.get(position)
                                    if (Constants.NEW_RELEASE_TYPE_WEBVIEW == item?.cta?.action) {
                                        val url = BuildConfig.WEB_VIEW_URL + "${item.cta?.pageUrl}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&domain_name=${item.domainName}&purchase_price=${item.originalPrice}&renewal_price=${item.renewalPrice}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.ON_BOARDING}"
                                        openWebViewFragment(this@OrderFragment, "", url)
                                    }
                                }
                            })
                    }
                }
            }.show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "$TAG onRequestPermissionResult")
        when (requestCode) {
            Constants.CONTACT_REQUEST_CODE -> {
                when {
                    grantResults.isEmpty() -> Log.d(TAG, "CONTACT_REQUEST_CODE :: User interaction was cancelled.")
                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        CoroutineScopeUtils().runTaskOnCoroutineBackground { getContactsFromStorage2(mActivity) }
                        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
                            mService?.getOrderPageInfo()
                            mService?.getAnalyticsData()
                        }
                    }
                    else -> {
                        mService?.getOrderPageInfo()
                        mService?.getAnalyticsData()
                    }
                }
            }
            Constants.IMAGE_PICK_REQUEST_CODE -> {
                when {
                    grantResults.isEmpty() -> Log.d(TAG, "IMAGE_PICK_REQUEST_CODE :: User interaction was cancelled.")
                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> openCameraWithoutCrop()
                }
            }
        }
    }

    private fun showDialogOrNot(){
        if (true == sIsInvitationShown) {
            showStaffInvitationDialog(StaticInstances.sStaffInvitation)
        } else {
            setupOrderPageInfoUI()
        }
    }

    private fun setupOrderPageInfoUI() {
        try {
            sOrderPageInfoResponse?.let { pageInfoResponse ->
                sOrderPageInfoStaticData = pageInfoResponse.mOrderPageStaticText
                sFetchingOrdersStr = sOrderPageInfoStaticData?.fetching_orders
                sDoubleClickToExitStr = sOrderPageInfoStaticData?.msg_double_click_to_exit
                StaticInstances.sOrderPageInfoStaticData = sOrderPageInfoStaticData
                StaticInstances.sSearchDomainUrl = pageInfoResponse.mDomainSearchUrl
                StaticInstances.sExploreDomainUrl = pageInfoResponse.mDomainExploreUrl
                val appTitleTextView: TextView? = mContentView?.findViewById(R.id.appTitleTextView)
                val pendingOrderTextView: TextView? = mContentView?.findViewById(R.id.pendingOrderTextView)
                val completedOrderTextView: TextView? = mContentView?.findViewById(R.id.completedOrderTextView)
                val takeOrderTextView: TextView? = mContentView?.findViewById(R.id.takeOrderTextView)
                val myOrdersHeadingTextView: TextView? = mContentView?.findViewById(R.id.myOrdersHeadingTextView)
                val noOrderLayout: View? = mContentView?.findViewById(R.id.noOrderLayout)
                val ordersLayout: View? = mContentView?.findViewById(R.id.ordersLayout)
                val orderLayout: View? = mContentView?.findViewById(R.id.orderLayout)
                val analyticsImageView: View? = mContentView?.findViewById(R.id.analyticsImageView)
                val searchImageView: View? = mContentView?.findViewById(R.id.searchImageView)
                appTitleTextView?.text = pageInfoResponse.mStoreInfo?.storeInfo?.name
                pendingOrderTextView?.text = sOrderPageInfoStaticData?.text_pending
                completedOrderTextView?.text = sOrderPageInfoStaticData?.text_completed
                ePosTextView?.text = sOrderPageInfoStaticData?.text_epos
                subHeadingTextView?.setHtmlData(sOrderPageInfoStaticData?.text_congratulations)
                myOrdersHeadingTextView?.text = sOrderPageInfoStaticData?.text_my_orders
                setupSideOptionMenu()
                swipeRefreshLayout?.isEnabled = true
                orderLayout?.visibility = View.VISIBLE
                if (pageInfoResponse.mIsZeroOrder.mIsActive) {
                    noOrderLayout?.visibility = View.VISIBLE
                    ordersLayout?.visibility = View.GONE
                    val noOrderHeadingTextView: TextView? = mContentView?.findViewById(R.id.noOrderHeadingTextView)
                    val noOrderSubHeadingTextView: TextView? = mContentView?.findViewById(R.id.noOrderSubHeadingTextView)
                    val shareStoreTextView: TextView? = mContentView?.findViewById(R.id.shareStoreTextView)
                    noOrderHeadingTextView?.text = sOrderPageInfoStaticData?.heading_zero_order
                    noOrderSubHeadingTextView?.text = sOrderPageInfoStaticData?.message_zero_order
                    shareStoreTextView?.text = sOrderPageInfoStaticData?.text_share_store
                } else {
                    noOrderLayout?.visibility = View.GONE
                    ordersLayout?.visibility = View.VISIBLE
                    swipeRefreshLayout?.isEnabled = true
                    Handler(Looper.getMainLooper()).postDelayed({ fetchLatestOrders(Constants.MODE_PENDING, sFetchingOrdersStr, mPendingPageCount) }, Constants.ORDER_DELAY_INTERVAL)
                }
                takeOrderTextView?.text = sOrderPageInfoStaticData?.text_payment_link
                analyticsImageView?.visibility = if (pageInfoResponse.mIsAnalyticsOrder) View.VISIBLE else View.GONE
                searchImageView?.visibility = if (pageInfoResponse.mIsSearchOrder) View.VISIBLE else View.GONE
                takeOrderTextView?.visibility = if (pageInfoResponse.mIsTakeOrder) View.VISIBLE else View.GONE
            }
            if(StaticInstances.sPermissionHashMap?.get("landing_cards") == true){
                mService?.getLandingPageCards()
            }else {
                myShortcutsRecyclerView?.visibility = View.GONE
                zeroOrderItemsRecyclerView?.visibility = View.GONE
                nextStepTextView?.visibility = View.GONE

            }
        } catch (e: Exception) {
            Log.e(TAG, "setupOrderPageInfoUI: ${e.message}", e)
        }
    }

    private fun setupSideOptionMenu() {
        mActivity?.let { context ->
            val optionsMenuImageView: ImageView? = mContentView?.findViewById(R.id.optionsMenuImageView)
            optionsMenuImageView?.visibility = if (isEmpty(sOrderPageInfoResponse?.optionMenuList)) View.GONE else View.VISIBLE
            optionsMenuImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_options_menu))
            optionsMenuImageView?.setOnClickListener {
                val wrapper = ContextThemeWrapper(mActivity, R.style.popupMenuStyle)
                val optionsMenu = PopupMenu(wrapper, optionsMenuImageView)
                optionsMenu.inflate(R.menu.menu_product_fragment)
                sOrderPageInfoResponse?.optionMenuList?.forEachIndexed { position, response ->
                    optionsMenu.menu?.add(Menu.NONE, position, Menu.NONE, response.mText)
                }
                optionsMenu.setOnMenuItemClickListener(this)
                optionsMenu.show()
            }
        }
    }

    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        val optionMenuItem = sOrderPageInfoResponse?.optionMenuList?.get(menuItem?.itemId ?: 0)
        when(optionMenuItem?.mAction) {
            Constants.NEW_RELEASE_TYPE_WEBVIEW -> openWebViewFragment(this, getString(R.string.help), WebViewUrls.WEB_VIEW_HELP, Constants.SETTINGS)
            Constants.ACTION_BOTTOM_SHEET -> if (Constants.PAGE_ORDER_NOTIFICATIONS == optionMenuItem.mPage) getOrderNotificationBottomSheet(AFInAppEventParameterName.IS_ORDER_PAGE)
        }
        return true
    }

    private fun showEPosBottomSheet() {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_take_order, it.findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                view?.run {
                    val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                    val lockImageView: View = findViewById(R.id.lockImageView)
                    val sendPaymentLinkView: View = findViewById(R.id.sendPaymentLinkTextView)
                    val sendPaymentLinkTextView: TextView = findViewById(R.id.sendPaymentLinkTextView1)
                    val takeOrderMessageTextView: TextView = findViewById(R.id.takeOrderMessageTextView)
                    val createNewBillTextView: TextView = findViewById(R.id.createNewBillTextView)
                    sendPaymentLinkTextView.apply {
                        text = sOrderPageInfoStaticData?.text_send_payment_link
                        sendPaymentLinkView.setOnClickListener {
                            sendPaymentLinkTextView.isEnabled = false
                            if (true == sOrderPageInfoResponse?.mPaymentLinkLocked?.mIsActive) {
                                openSubscriptionLockedUrlInBrowser(sOrderPageInfoResponse?.mPaymentLinkLocked?.mUrl ?: "")
                            } else {
                                showPaymentLinkBottomSheet()
                                Log.d(TAG, "showEPosBottomSheet: ")
                            }
                            bottomSheetDialog.dismiss()
                        }
                    }
                    lockImageView.visibility = if (false == sOrderPageInfoResponse?.mIsSubscriptionDone) View.VISIBLE else View.GONE
                    bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                    takeOrderMessageTextView.setHtmlData(sOrderPageInfoStaticData?.bottom_sheet_message_payment_link)
                    createNewBillTextView.apply {
                        text = sOrderPageInfoStaticData?.bottom_sheet_create_a_new_bill
                        setOnClickListener {
                            if (StaticInstances.sIsShareStoreLocked) {
                                getLockedStoreShareDataServerCall(Constants.MODE_CREATE_BILL)
                                return@setOnClickListener
                            }
                            createNewBillTextView.isEnabled = false
                            bottomSheetDialog.dismiss()
                            launchFragment(CommonWebViewFragment().newInstance("", "${BuildConfig.WEB_VIEW_URL}${WebViewUrls.WEB_VIEW_TAKE_A_ORDER}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"), true)
                        }
                    }
                }
            }.show()
        }
    }

    private fun pushProfileToCleverTap() {
        try {
            val storeResponse = sOrderPageInfoResponse?.mStoreInfo
            storeResponse?.run {
                val cleverTapProfile = CleverTapProfile()
                cleverTapProfile.mShopName = storeInfo.name
                var businessTypeStr = ""
                storeBusiness?.forEachIndexed { _, businessResponse -> businessTypeStr += "$businessResponse ," }
                cleverTapProfile.mShopCategory = businessTypeStr
                cleverTapProfile.mPhone = storeOwner?.phone
                cleverTapProfile.mIdentity = storeOwner?.phone
                cleverTapProfile.mLat = storeAddress?.latitude
                cleverTapProfile.mLong = storeAddress?.longitude
                cleverTapProfile.mAddress = storeAddress?.let {
                    "${it.address1}, ${it.googleAddress}, ${it.pinCode}"
                }
                AppEventsManager.pushCleverTapProfile(cleverTapProfile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "pushProfileToCleverTap: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "pushProfileToCleverTap",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
        }
    }

    override fun onBackPressed(): Boolean {
        if (mIsDoublePressToExit) mActivity?.finish()
        showShortSnackBar(if (isNotEmpty(sDoubleClickToExitStr)) sDoubleClickToExitStr else getString(R.string.msg_back_press))
        mIsDoublePressToExit = true
        Handler(Looper.getMainLooper()).postDelayed(
            { mIsDoublePressToExit = false },
            Constants.BACK_PRESS_INTERVAL
        )
        return true
    }

    override fun onSearchDialogContinueButtonClicked(inputOrderId: String, inputMobileNumber: String) {
        try {
            sOrderIdString = inputOrderId
            sMobileNumberString = inputMobileNumber
            val request = SearchOrdersRequest(if (isNotEmpty(sOrderIdString)) sOrderIdString.toLong() else 0, sMobileNumberString, 1)
            if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog()
            showProgressDialog(mActivity)
            mService?.getSearchOrders(request)
        } catch (e: Exception) {
            showToast(mActivity?.getString(R.string.something_went_wrong))
            Log.e(TAG, "onSearchDialogContinueButtonClicked: ${e.message}", e)
        }
    }

    private fun fetchLatestOrders(mode: String, fetchingOrderStr: String?, page: Int = 1, showProgressDialog: Boolean = true) {
        if (showProgressDialog) if (isNotEmpty(fetchingOrderStr)) showCancellableProgressDialog(mActivity, fetchingOrderStr)
        val request = OrdersRequest(mode, page)
        mService?.getOrders(request)
    }

    override fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showToast(validateOtpErrorResponse.mMessage)
        }
    }

    override fun onRefresh() {
        sOrderList.clear()
        sCompletedOrderList.clear()
        mCompletedPageCount = 1
        mPendingPageCount = 1
        fetchLatestOrders(Constants.MODE_PENDING,
            sFetchingOrdersStr,
            mPendingPageCount
        )
        mService?.getOrderPageInfo()

        mService?.getAnalyticsData()
    }

    override fun onOrderCheckBoxChanged(isChecked: Boolean, item: OrderItemResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (Constants.TEXT_YES != PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN)) {
                if (Constants.DS_PAID_ONLINE == item?.displayStatus || Constants.DS_PREPAID_PICKUP_READY == item?.displayStatus || Constants.DS_PREPAID_DELIVERY_READY == item?.displayStatus) {
                    onDontShowDialogPositiveButtonClicked(item)
                } else if (isChecked)
                    showDontShowDialog(item, sOrderPageInfoStaticData)
            } else onDontShowDialogPositiveButtonClicked(item)
        }
    }

    override fun onDontShowDialogPositiveButtonClicked(item: OrderItemResponse?) {
        val request = CompleteOrderRequest(item?.orderId?.toLong())
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
            showProgressDialog(mActivity)
            mService?.completeOrder(request)
        }
    }

    override fun onOrderItemCLickChanged(item: OrderItemResponse?) {
        var isNewOrder = false
        if (Constants.DS_NEW == item?.displayStatus) {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_VERIFY_ORDER_SEEN,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.ORDER_ID to "${item.orderId}",
                    AFInAppEventParameterName.PHONE to item.phone,
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.ORDER_TYPE to if (item.orderType == Constants.ORDER_TYPE_PICK_UP) "pickup" else "delivery",
                    AFInAppEventParameterName.IS_MERCHANT to "1"
                )
            )
            val request = UpdateOrderStatusRequest(
                item.orderId.toLong(),
                Constants.StatusSeenByMerchant.toLong()
            )
            mService?.updateOrderStatus(request)
            isNewOrder = true
        }
        launchFragment(OrderDetailFragment.newInstance(item?.orderHash.toString(), isNewOrder), true)
    }

    override fun onDestroy() {
        super.onDestroy()
        sOrderPageInfoResponse = null
    }

    private fun showPaymentLinkBottomSheet() {
        mActivity?.let {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SEND_PAYMENT_LINK,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
            )
            mPaymentLinkBottomSheet = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_payment_link, it.findViewById(R.id.bottomSheetContainer))
            mPaymentLinkBottomSheet?.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                setCancelable(true)
                view?.run {
                    val staticText = StaticInstances.sOrderPageInfoStaticData
                    val billCameraImageView: View = findViewById(R.id.billCameraImageView)
                    val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                    val amountEditText: EditText = findViewById(R.id.amountEditText)
                    val sendLinkTextView: TextView = findViewById(R.id.sendLinkTextView)
                    val sendBillToCustomerTextView: TextView = findViewById(R.id.sendBillToCustomerTextView)
                    val customerCanPayUsingTextView: TextView = findViewById(R.id.customerCanPayUsingTextView)
                    sendLinkTextView.text = staticText?.text_send_link
                    sendBillToCustomerTextView.setHtmlData(staticText?.bottom_sheet_heading_send_link)
                    customerCanPayUsingTextView.setHtmlData(staticText?.bottom_sheet_message_customer_pay)
                    bottomSheetClose.setOnClickListener { mPaymentLinkBottomSheet?.dismiss() }
                    billCameraImageView.setOnClickListener {
                        mPaymentLinkAmountStr = amountEditText.text.toString()
                        mPaymentLinkBottomSheet?.dismiss()
                        openCameraWithoutCrop()
                    }
                    sendLinkTextView.setOnClickListener {
                        if (isEmpty(amountEditText.text.toString())) {
                            amountEditText.requestFocus()
                            amountEditText.error = staticText?.error_mandatory_field
                            return@setOnClickListener
                        }
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_SEND_LINK,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)))
                        showPaymentLinkSelectionDialog(amountEditText.text.toString())
                    }
                    amountEditText.requestFocus()
                    amountEditText.showKeyboard()
                }
            }?.show()
        }
    }

    override fun onWhatsAppIconClicked() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mPaymentLinkBottomSheet?.dismiss()
            } catch (e: Exception) {
                Log.e(TAG, "onWhatsAppIconClicked: ${e.message}", e)
            }
        }
    }

    override fun onSMSIconClicked() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mPaymentLinkBottomSheet?.dismiss()
            } catch (e: Exception) {
                Log.e(TAG, "onWhatsAppIconClicked: ${e.message}", e)
            }
        }
    }

    override fun onImageSelectionResultFileAndUri(fileUri: Uri?, file: File?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                launchFragment(SendBillFragment.newInstance(fileUri, file, mPaymentLinkAmountStr ?: ""), true)
            } catch (e: Exception) {
                Log.e(TAG, "onImageSelectionResultUri: ${e.message}", e)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        "Exception Point" to "onImageSelectionResultUri",
                        "Exception Message" to e.message,
                        "Exception Logs" to e.toString()
                    )
                )
            }
        }
    }

    override fun onLockedStoreShareSuccessResponse(lockedShareResponse: LockedStoreShareResponse) = showLockedStoreShareBottomSheet(lockedShareResponse)

    override fun checkStaffInviteResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                var sCheckStaffInviteResponse = Gson().fromJson<StaffMemberDetailsResponse>(commonResponse.mCommonDataStr, StaffMemberDetailsResponse::class.java)
                mIsInvitationShown = sCheckStaffInviteResponse.mIsInvitationAvailable
                Log.i("isInvitationShownOrders", sCheckStaffInviteResponse?.mIsInvitationAvailable.toString())
            }
        }
    }
}