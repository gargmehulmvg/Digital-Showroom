package com.digitaldukaan.fragments

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
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
import com.digitaldukaan.adapters.*
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.*
import com.digitaldukaan.models.dto.CleverTapProfile
import com.digitaldukaan.models.request.*
import com.digitaldukaan.models.response.*
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.OrderFragmentService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.landing_page_cards_item.*
import kotlinx.android.synthetic.main.layout_analytics.*
import kotlinx.android.synthetic.main.layout_home_fragment.*
import kotlinx.android.synthetic.main.layout_home_fragment.analyticsContainer
import kotlinx.android.synthetic.main.layout_home_fragment.analyticsImageView
import kotlinx.android.synthetic.main.layout_home_fragment.orderLayout
import kotlinx.android.synthetic.main.layout_order_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.slybeaver.slycalendarview.SlyCalendarDialog
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class OrderFragment : BaseFragment(), IHomeServiceInterface, PopupMenu.OnMenuItemClickListener,
    SwipeRefreshLayout.OnRefreshListener, IOrderListItemListener, ILeadsListItemListener {

    private var ePosTextView: TextView? = null
    private var searchImageView: ImageView? = null
    private var toolbarSearchImageView: ImageView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var mIsNewUserLogin = false
    private var mIsDoublePressToExit = false
    private var domainExpiryTextView: TextView? = null
    private var ordersRecyclerView: RecyclerView? = null
    private var completedOrdersRecyclerView: RecyclerView? = null
    private var mOrderAdapter: OrderAdapterV2? = null
    private var mLeadsAdapter: LeadsAdapter? = null
    private var mCompletedOrderAdapter: OrderAdapterV2? = null
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
    private var mIsCustomDomainTopViewHide: Boolean = false
    private var mLeadsFilterAdapter: LeadsFilterBottomSheetAdapter? = null
    private var mLeadsFilterList : ArrayList<LeadsFilterListItemResponse> = ArrayList()
    private var mLeadsFilterResponse: LeadsFilterResponse? = null
    private var mLeadsCartTypeSelection = Constants.CART_TYPE_DEFAULT
    private var mIsLeadsTabSelected = false
    private var mIsLeadsFilterReset = true
    private var mLeadsFilterStartDate = ""
    private var mLeadsFilterEndDate = ""
    private var leadsFilterSortType = Constants.SORT_TYPE_DESCENDING
    private var mLeadsFilterRequest: LeadsListRequest = LeadsListRequest("", "", "", "", Constants.SORT_TYPE_DESCENDING, Constants.CART_TYPE_DEFAULT)

    companion object {
        var sOrderPageInfoResponse: OrderPageInfoResponse? = null
        private var sOrderPageInfoStaticData: OrderPageStaticTextResponse? = null
        private var sAnalyticsResponse: AnalyticsResponse? = null
        private var sDoubleClickToExitStr: String? = ""
        private var sFetchingOrdersStr: String? = ""
        private var sMobileNumberString = ""
        private var sOrderIdString = ""
        private const val LEADS_FILTER_TYPE_CUSTOM_DATE = "0"
        private var sIsMorePendingOrderAvailable = false
        private var sIsMoreCompletedOrderAvailable = false
        private var sOrderList: ArrayList<OrderItemResponse> = ArrayList()
        private var sCompletedOrderList: ArrayList<OrderItemResponse> = ArrayList()
        private var sLeadsList: ArrayList<LeadsResponse> = ArrayList()

        fun newInstance(isNewUserLogin: Boolean = false, isClearOrderPageResponse: Boolean = false): OrderFragment {
            val fragment = OrderFragment()
            fragment.mIsNewUserLogin = isNewUserLogin
            if (isClearOrderPageResponse) this.sOrderPageInfoResponse = null
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
            if (null == StaticInstances.sCustomDomainBottomSheetResponse) {
                mIsCustomDomainTopViewHide = false
                mService?.getCustomDomainBottomSheetData()
            } else
                StaticInstances.sCustomDomainBottomSheetResponse?.let { response -> showDomainPurchasedBottomSheet(response, false) }
        }
        mIsLeadsTabSelected = false
        return mContentView
    }

    private fun initializeViews() {
        ePosTextView = mContentView?.findViewById(R.id.ePosTextView)
        searchImageView = mContentView?.findViewById(R.id.searchImageView)
        toolbarSearchImageView = mContentView?.findViewById(R.id.toolbarSearchImageView)
        swipeRefreshLayout = mContentView?.findViewById(R.id.swipeRefreshLayout)
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
        sLeadsList.clear()
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
                if (!mIsLeadsTabSelected) {
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
            }
        })
        setupOrdersRecyclerView()
        ordersRecyclerView?.addItemDecoration(StickyRecyclerHeadersDecoration(mOrderAdapter))
        setupCompletedOrdersRecyclerView()
        completedOrdersRecyclerView?.addItemDecoration(StickyRecyclerHeadersDecoration(mCompletedOrderAdapter))
        Log.d(TAG, "onViewCreated: OrderFragment called")
        Log.d(TAG, "onViewCreated: OrderFragment sIsInvitationAvailable :: $sIsInvitationAvailable")
        if (sIsInvitationAvailable) showStaffInvitationDialog()
    }

    private fun setupOrdersRecyclerView() {
        ordersRecyclerView?.apply {
            isNestedScrollingEnabled = false
            mActivity?.let { context -> mOrderAdapter = OrderAdapterV2(context, sOrderList) }
            mOrderAdapter?.setCheckBoxListener(this@OrderFragment)
            layoutManager = LinearLayoutManager(mActivity)
            adapter = mOrderAdapter
        }
    }

    private fun setupCompletedOrdersRecyclerView() {
        completedOrdersRecyclerView?.apply {
            isNestedScrollingEnabled = false
            mActivity?.let { context ->
                mCompletedOrderAdapter = OrderAdapterV2(context, sCompletedOrderList)
            }
            mCompletedOrderAdapter?.setCheckBoxListener(this@OrderFragment)
            layoutManager = LinearLayoutManager(mActivity)
            adapter = mCompletedOrderAdapter
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
                val ordersResponse = Gson().fromJson(getOrderResponse.mCommonDataStr, OrdersResponse::class.java)
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
                val ordersResponse = Gson().fromJson(getOrderResponse.mCommonDataStr, OrdersResponse::class.java)
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
                sAnalyticsResponse = Gson().fromJson(commonResponse.mCommonDataStr, AnalyticsResponse::class.java)
                setupAnalyticsUI()
            }
        }
    }

    override fun onOrderPageInfoResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                sOrderPageInfoResponse = Gson().fromJson(commonResponse.mCommonDataStr, OrderPageInfoResponse::class.java)
                setupOrderPageInfoUI()
                pushProfileToCleverTap()
            }
        }
    }

    override fun onSearchOrdersResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (true == swipeRefreshLayout?.isRefreshing) swipeRefreshLayout?.isRefreshing = false
            if (commonResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson(commonResponse.mCommonDataStr, OrdersResponse::class.java)
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
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val customDomainBottomSheetResponse = Gson().fromJson(commonResponse.mCommonDataStr, CustomDomainBottomSheetResponse::class.java)
                StaticInstances.sCustomDomainBottomSheetResponse = customDomainBottomSheetResponse
                showDomainPurchasedBottomSheet(customDomainBottomSheetResponse, isNoDomainFoundLayout = false, hideTopView = mIsCustomDomainTopViewHide)
            }
        }
    }

    override fun onLandingPageCardsResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                val landingPageCardsResponse = Gson().fromJson(commonResponse.mCommonDataStr, LandingPageCardsResponse::class.java)
                Log.d(TAG, "onLandingPageCardsResponse: $landingPageCardsResponse")
                mIsAllStepsCompleted = false
                StaticInstances.sIsShareStoreLocked = landingPageCardsResponse?.isShareStoreLocked ?: false
                landingPageCardsResponse?.zeroOrderItemsList?.forEachIndexed { _, itemsResponse ->
                    mIsAllStepsCompleted = itemsResponse?.completed?.isCompleted ?: false
                }
                val nextStepTextView: TextView? = mContentView?.findViewById(R.id.nextStepTextView)
                val domainExpiryContainer: View? = mContentView?.findViewById(R.id.domainExpiryContainer)
                if (mIsAllStepsCompleted) {
                    zeroOrderItemsRecyclerView?.visibility = View.GONE
                    if (true == StaticInstances.sPermissionHashMap?.get(Constants.MY_SHORTCUTS)) {
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
                                                                val accountInfoResponse = Gson().fromJson(it.mCommonDataStr, AccountInfoResponse::class.java)
                                                                StaticInstances.sAccountPageSettingsStaticData = accountInfoResponse?.mAccountStaticText
                                                                StaticInstances.sAppStoreServicesResponse = accountInfoResponse?.mStoreInfo?.storeServices
                                                                launchFragment(MoreControlsFragment.newInstance(accountInfoResponse?.mAccountStaticText), true)
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
                            }, Constants.TIMER_AUTO_DISMISS_PROGRESS_DIALOG)
                        }, Constants.TIMER_AUTO_DISMISS_PROGRESS_DIALOG)
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
                            if (!StaticInstances.sSuggestedDomainsListFetchedFromServer) {
                                StaticInstances.sSuggestedDomainsListFetchedFromServer = true
                                mService?.getDomainSuggestionList(mActivity?.resources?.getInteger(R.integer.custom_domain_count) ?: 4)
                            }
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
            } else {
                StaticInstances.sCustomDomainBottomSheetResponse = null
                StaticInstances.sSuggestedDomainsList = null
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
            domainExpiryContainer?.id -> {
                if (null == StaticInstances.sCustomDomainBottomSheetResponse) {
                    mIsCustomDomainTopViewHide = true
                    showProgressDialog(mActivity)
                    mService?.getCustomDomainBottomSheetData()
                } else
                    StaticInstances.sCustomDomainBottomSheetResponse?.let { response -> showDomainPurchasedBottomSheet(response, isNoDomainFoundLayout = false, hideTopView = true) }
            }
            searchImageView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SEARCH_INTENT,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                if (mIsLeadsTabSelected) {
                    launchFragment(LeadsSearchFragment.newInstance(), true)
                } else showSearchDialog(sOrderPageInfoStaticData, sMobileNumberString, sOrderIdString)
            }
            toolbarSearchImageView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SEARCH_INTENT,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                if (mIsLeadsTabSelected) {
                    launchFragment(LeadsSearchFragment.newInstance(), true)
                } else showSearchDialog(sOrderPageInfoStaticData, sMobileNumberString, sOrderIdString)
            }
            ePosContainer?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_TAKE_ORDER,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                showEPosBottomSheet()
            }
            myOrdersHeadingTextView?.id -> {
                mIsLeadsTabSelected = false
                noLeadsLayout?.visibility = View.GONE
                noOrderLayout?.visibility = View.GONE
                setupTabLayout(myOrdersHeadingTextView, myLeadsHeadingTextView)
                setupOrdersRecyclerView()
                setupCompletedOrdersRecyclerView()
                completedOrdersRecyclerView?.visibility = View.VISIBLE
                with(View.VISIBLE) {
                    val pendingOrderTextView: TextView? = mContentView?.findViewById(R.id.pendingOrderTextView)
                    val completedOrderTextView: TextView? = mContentView?.findViewById(R.id.completedOrderTextView)
                    pendingOrderTextView?.visibility = this
                    completedOrderTextView?.visibility = this
                }
                val abandonedCartTextView: TextView? = mContentView?.findViewById(R.id.abandonedCartTextView)
                val activeCartTextView: TextView? = mContentView?.findViewById(R.id.activeCartTextView)
                val filterImageView: ImageView? = mContentView?.findViewById(R.id.filterImageView)
                with(View.GONE) {
                    abandonedCartTextView?.visibility = this
                    activeCartTextView?.visibility = this
                    filterImageView?.visibility = this
                    filterRedDotImageView?.visibility = this
                }
            }
            myLeadsHeadingTextView?.id -> {
                if (false == StaticInstances.sPermissionHashMap?.get(Constants.ABANDONED_CART)) {
                    val url = "${sOrderPageInfoResponse?.mAbandonedCartLockedUrl}?storeid=${PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)}&token=${PrefsManager.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.LANDING_PAGE}"
                    openWebViewFragmentV3(this@OrderFragment, "", url)
                    return
                }
                noLeadsLayout?.visibility = View.GONE
                noOrderLayout?.visibility = View.GONE
                mLeadsCartTypeSelection = Constants.CART_TYPE_DEFAULT
                mLeadsFilterResponse = null
                mLeadsFilterList = ArrayList()
                mIsLeadsTabSelected = true
                setupTabLayout(myLeadsHeadingTextView, myOrdersHeadingTextView)
                ordersRecyclerView?.apply {
                    isNestedScrollingEnabled = false
                    mActivity?.let { context -> mLeadsAdapter = LeadsAdapter(context, sLeadsList, this@OrderFragment) }
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = null
                    adapter = mLeadsAdapter
                }
                completedOrdersRecyclerView?.visibility = View.GONE
                val pendingOrderTextView: TextView? = mContentView?.findViewById(R.id.pendingOrderTextView)
                val completedOrderTextView: TextView? = mContentView?.findViewById(R.id.completedOrderTextView)
                with(View.GONE) {
                    pendingOrderTextView?.visibility = this
                    completedOrderTextView?.visibility = this
                }
                val abandonedCartTextView: TextView? = mContentView?.findViewById(R.id.abandonedCartTextView)
                val activeCartTextView: TextView? = mContentView?.findViewById(R.id.activeCartTextView)
                val filterImageView: ImageView? = mContentView?.findViewById(R.id.filterImageView)
                with(View.VISIBLE) {
                    abandonedCartTextView?.visibility = this
                    activeCartTextView?.visibility = this
                    filterImageView?.visibility = this
                }
                mLeadsFilterRequest = LeadsListRequest(
                    userName = "",
                    startDate = "",
                    endDate = "",
                    userPhone = "",
                    sortType = Constants.SORT_TYPE_DESCENDING,
                    cartType = mLeadsCartTypeSelection
                )
                mService?.getCartsByFilters(mLeadsFilterRequest)
                mActivity?.let { context ->
                    abandonedCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.slight_curve_light_grey_background_without_padding)
                    activeCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.slight_curve_light_grey_background_without_padding)
                }
            }
            abandonedCartTextView?.id -> {
                if (Constants.CART_TYPE_ABANDONED == mLeadsCartTypeSelection) {
                    mActivity?.let { context ->
                        abandonedCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.slight_curve_light_grey_background_without_padding)
                        activeCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.slight_curve_light_grey_background_without_padding)
                    }
                    mLeadsCartTypeSelection = Constants.CART_TYPE_DEFAULT
                    mLeadsFilterRequest.cartType = Constants.CART_TYPE_DEFAULT
                } else {
                    mActivity?.let { context ->
                        abandonedCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.selected_chip_blue_border_bluish_background)
                        activeCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.slight_curve_light_grey_background_without_padding)
                    }
                    mLeadsCartTypeSelection = Constants.CART_TYPE_ABANDONED
                    mLeadsFilterRequest.cartType = Constants.CART_TYPE_ABANDONED
                }
                mService?.getCartsByFilters(mLeadsFilterRequest)
            }
            activeCartTextView?.id -> {
                if (Constants.CART_TYPE_ACTIVE == mLeadsCartTypeSelection) {
                    mActivity?.let { context ->
                        abandonedCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.slight_curve_light_grey_background_without_padding)
                        activeCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.slight_curve_light_grey_background_without_padding)
                    }
                    mLeadsCartTypeSelection = Constants.CART_TYPE_DEFAULT
                    mLeadsFilterRequest.cartType = Constants.CART_TYPE_DEFAULT
                } else {
                    mActivity?.let { context ->
                        abandonedCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.slight_curve_light_grey_background_without_padding)
                        activeCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.selected_chip_blue_border_bluish_background)
                    }
                    mLeadsCartTypeSelection = Constants.CART_TYPE_ACTIVE
                    mLeadsFilterRequest.cartType = Constants.CART_TYPE_ACTIVE
                }
                mService?.getCartsByFilters(mLeadsFilterRequest)
            }
            filterImageView?.id -> {
                if (null == mLeadsFilterResponse) {
                    val request = LeadsFilterOptionsRequest(cartType = -1, startDate = "", endDate = "", sortType = Constants.SORT_TYPE_DESCENDING)
                    mService?.getCartFilterOptions(request)
                } else showLeadsFilterBottomSheet()
            }
        }
    }

    private fun setupTabLayout(selectedTextView: TextView?, unSelectedTextView: TextView?) {
        mActivity?.let { context ->
            selectedTextView?.apply {
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                background = ContextCompat.getDrawable(context, R.drawable.ripple_rect_grey_blue_background)
            }
            unSelectedTextView?.apply {
                typeface = Typeface.DEFAULT
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.WHITE)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "$TAG onRequestPermissionResult")
        when (requestCode) {
            Constants.REQUEST_CODE_CONTACT -> {
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
                val myLeadsHeadingTextView: TextView? = mContentView?.findViewById(R.id.myLeadsHeadingTextView)
                val noOrderLayout: View? = mContentView?.findViewById(R.id.noOrderLayout)
                val ordersLayout: View? = mContentView?.findViewById(R.id.ordersLayout)
                val orderLayout: View? = mContentView?.findViewById(R.id.orderLayout)
                val analyticsImageView: View? = mContentView?.findViewById(R.id.analyticsImageView)
                val searchImageView: View? = mContentView?.findViewById(R.id.searchImageView)
                appTitleTextView?.text = pageInfoResponse.mStoreInfo?.storeInfo?.name
                pendingOrderTextView?.text = sOrderPageInfoStaticData?.text_pending
                completedOrderTextView?.text = sOrderPageInfoStaticData?.text_completed
                ePosTextView?.text = sOrderPageInfoStaticData?.text_epos
                myOrdersHeadingTextView?.text = sOrderPageInfoStaticData?.text_my_orders
                myLeadsHeadingTextView?.text = sOrderPageInfoStaticData?.heading_leads
                myLeadsHeadingTextView?.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    if (true == StaticInstances.sPermissionHashMap?.get(Constants.ABANDONED_CART)) 0 else R.drawable.ic_subscription_locked_black_small,
                    0,
                    R.drawable.ic_red_dot,
                    0
                )
                if (true == StaticInstances.sPermissionHashMap?.get(Constants.ABANDONED_CART)) {
                    myLeadsHeadingTextView?.gravity = Gravity.CENTER
                }
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
                    Handler(Looper.getMainLooper()).postDelayed({ fetchLatestOrders(Constants.MODE_PENDING, sFetchingOrdersStr, mPendingPageCount) }, Constants.TIMER_ORDER_DELAY)
                }
                takeOrderTextView?.text = sOrderPageInfoStaticData?.text_payment_link
                analyticsImageView?.visibility = if (pageInfoResponse.mIsAnalyticsOrder) View.VISIBLE else View.GONE
                searchImageView?.visibility = if (pageInfoResponse.mIsSearchOrder) View.VISIBLE else View.GONE
                takeOrderTextView?.visibility = if (pageInfoResponse.mIsTakeOrder) View.VISIBLE else View.GONE
            }
            if (true == StaticInstances.sPermissionHashMap?.get(Constants.LANDING_CARDS)) {
                mService?.getLandingPageCards()
            } else {
                val myShortcutsRecyclerView: View? = mContentView?.findViewById(R.id.myShortcutsRecyclerView)
                val zeroOrderItemsRecyclerView: View? = mContentView?.findViewById(R.id.zeroOrderItemsRecyclerView)
                val nextStepTextView: View? = mContentView?.findViewById(R.id.nextStepTextView)
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
                val wrapper = ContextThemeWrapper(mActivity, R.style.PopupMenuStyle)
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
        if (showProgressDialog) if (isNotEmpty(fetchingOrderStr)) showCancellableProgressDialog(mActivity)
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
        if (!mIsLeadsTabSelected) {
            sOrderList.clear()
            sCompletedOrderList.clear()
            mCompletedPageCount = 1
            mPendingPageCount = 1
            fetchLatestOrders(Constants.MODE_PENDING, sFetchingOrdersStr, mPendingPageCount)
            mService?.getOrderPageInfo()
            mService?.getAnalyticsData()
        } else {
            mActivity?.let { context ->
                abandonedCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.slight_curve_grey_background_without_padding)
                activeCartTextView?.background = ContextCompat.getDrawable(context, R.drawable.slight_curve_grey_background_without_padding)
            }
            mLeadsCartTypeSelection = Constants.CART_TYPE_DEFAULT
            resetLeadsDateFilter()
            mLeadsFilterRequest = LeadsListRequest(
                userName = "",
                startDate = "",
                endDate = "",
                userPhone = "",
                sortType = Constants.SORT_TYPE_DESCENDING,
                cartType = mLeadsCartTypeSelection
            )
            mService?.getCartsByFilters(mLeadsFilterRequest)
        }
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
        launchFragment(OrderDetailFragment.newInstance(item?.orderHash.toString(), isNewOrder), addBackStack = true, isFragmentAdd = true)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (false == mActivity?.isDestroyed) sOrderPageInfoResponse = null
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

    override fun onCheckStaffInviteResponse() {
        showProgressDialog(mActivity)
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val response = RetrofitApi().getServerCallObject()?.getStaffMembersDetails(getStringDataFromSharedPref(Constants.STORE_ID))
                response?.let { it ->
                    val staffMemberDetailsResponse = Gson().fromJson(it.body()?.mCommonDataStr, CheckStaffInviteResponse::class.java)
                    blurBottomNavBarContainer?.visibility = View.INVISIBLE
                    stopProgress()
                    if (null != staffMemberDetailsResponse) {
                        Log.d(TAG, "StaticInstances.sPermissionHashMap: ${StaticInstances.sPermissionHashMap}")
                        StaticInstances.sPermissionHashMap = null
                        StaticInstances.sPermissionHashMap = staffMemberDetailsResponse.permissionsMap
                        staffMemberDetailsResponse.permissionsMap?.let { map -> launchScreenFromPermissionMap(map) }
                    } else {
                        launchFragment(newInstance(), true)
                    }
                }
                Log.d(TAG, "StaticInstances.sPermissionHashMap :: ${StaticInstances.sPermissionHashMap}")
            } catch (e: Exception) {
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    override fun checkStaffInviteResponse(commonResponse: CommonApiResponse) = Unit

    override fun onGetCartsByFiltersResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (true == swipeRefreshLayout?.isRefreshing) swipeRefreshLayout?.isRefreshing = false
            if (commonResponse.mIsSuccessStatus) {
                val listType = object : TypeToken<ArrayList<LeadsResponse>>() {}.type
                sLeadsList = ArrayList()
                sLeadsList = Gson().fromJson(commonResponse.mCommonDataStr, listType)
                val noLeadsLayout: View? = mContentView?.findViewById(R.id.noLeadsLayout)
                val noOrderLayout: View? = mContentView?.findViewById(R.id.noOrderLayout)
                val pendingOrderTextView: View? = mContentView?.findViewById(R.id.pendingOrderTextView)
                val completedOrderTextView: View? = mContentView?.findViewById(R.id.completedOrderTextView)
                if (isEmpty(sLeadsList)) {
                    noLeadsLayout?.visibility = View.VISIBLE
                    noOrderLayout?.visibility = View.GONE
                } else {
                    with(View.GONE) {
                        noLeadsLayout?.visibility = this
                        noOrderLayout?.visibility = this
                    }
                    with(View.VISIBLE) {
                        ordersRecyclerView?.visibility = this
                    }
                    sLeadsList.forEachIndexed { _, itemResponse -> itemResponse.updatedDate = getDateFromOrderString(itemResponse.lastUpdateOn) }
                    ordersRecyclerView?.removeItemDecorationAt(0)
                    ordersRecyclerView?.addItemDecoration(StickyRecyclerHeadersDecoration(mLeadsAdapter))
                }
                pendingOrderTextView?.visibility = View.GONE
                completedOrderTextView?.visibility = View.GONE
                mLeadsAdapter?.setLeadsList(sLeadsList)
            }
        }
    }

    override fun onCartFilterOptionsResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                mLeadsFilterResponse = Gson().fromJson(commonResponse.mCommonDataStr, LeadsFilterResponse::class.java)
                mLeadsFilterResponse?.filterList?.forEachIndexed { _, itemResponse ->
                    if (isNotEmpty(itemResponse.heading))
                        mLeadsFilterList.add(itemResponse)
                }
                mLeadsFilterList.forEachIndexed { _, itemResponse ->
                    if (Constants.LEADS_FILTER_TYPE_SORT == itemResponse.type) {
                        itemResponse.filterOptionsList[0].isSelected = true
                        return@forEachIndexed
                    }
                }
                mIsLeadsFilterReset = true
                showLeadsFilterBottomSheet()
            }
        }
    }

    override fun onLeadsItemCLickedListener(item: LeadsResponse?) = launchFragment(LeadDetailFragment.newInstance(item), addBackStack = true, isFragmentAdd = true)

    private fun showLeadsFilterBottomSheet() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.let {
                    val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                    val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_leads_filter, it.findViewById(R.id.bottomSheetContainer))
                    bottomSheetDialog.apply {
                        setContentView(view)
                        setCancelable(true)
                        view.run {
                            val clearFilterTextView: TextView = findViewById(R.id.clearFilterTextView)
                            val doneTextView: TextView = findViewById(R.id.doneTextView)
                            val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                            mLeadsFilterResponse?.staticText.let { staticText ->
                                doneTextView.text = staticText?.text_done
                                clearFilterTextView.apply {
                                    text = staticText?.text_clear_filter
                                    if (mIsLeadsFilterReset) {
                                        alpha = 0.5f
                                        isEnabled = false
                                    } else {
                                        alpha = 1f
                                        isEnabled = true
                                    }
                                }
                            }
                            clearFilterTextView.setOnClickListener {
                                clearFilterTextView.apply {
                                    mIsLeadsFilterReset = true
                                    alpha = 0.5f
                                    isEnabled = false
                                }
                                mLeadsFilterList.forEachIndexed { position, itemResponse ->
                                    if (Constants.LEADS_FILTER_TYPE_SORT == itemResponse.type) {
                                        itemResponse.filterOptionsList.forEachIndexed { pos, filterItem ->
                                            filterItem.isSelected = (0 == pos)
                                        }
                                    } else {
                                        itemResponse.filterOptionsList.forEachIndexed { _, filterItem ->
                                            with(filterItem) {
                                                isSelected = false
                                                customDateRangeStr = ""
                                            }
                                        }
                                    }
                                    mLeadsFilterAdapter?.notifyItemChanged(position)
                                }
                                mLeadsFilterEndDate = ""
                                mLeadsFilterStartDate = ""
                            }
                            doneTextView.setOnClickListener {
                                val filterRedDotImageView: View? = mContentView?.findViewById(R.id.filterRedDotImageView)
                                filterRedDotImageView?.visibility = if (mIsLeadsFilterReset) View.GONE else View.VISIBLE
                                mLeadsFilterRequest.apply {
                                    startDate = mLeadsFilterStartDate
                                    endDate = mLeadsFilterEndDate
                                    sortType = leadsFilterSortType
                                    cartType = mLeadsCartTypeSelection
                                }
                                mService?.getCartsByFilters(mLeadsFilterRequest)
                                (this@apply).dismiss()
                            }
                            recyclerView.apply {
                                isNestedScrollingEnabled = false
                                layoutManager = LinearLayoutManager(mActivity)
                                Log.d(TAG, "showLeadsFilterBottomSheet: list :: $mLeadsFilterList")
                                mLeadsFilterAdapter = LeadsFilterBottomSheetAdapter(mActivity, mLeadsFilterList, object : ILeadsFilterItemClickListener {

                                        override fun onLeadsFilterItemClickListener(item: LeadsFilterOptionsItemResponse?, filterType:String?) {
                                            Log.d(TAG, "filterType :: $filterType , item :: $item")
                                            var recyclerRedrawPosition = 0
                                            mIsLeadsFilterReset = false
                                            clearFilterTextView.apply {
                                                alpha = 1f
                                                isEnabled = true
                                            }
                                            mLeadsFilterList.forEachIndexed { position, itemResponse ->
                                                if (filterType == itemResponse.type) {
                                                    recyclerRedrawPosition = position
                                                    return@forEachIndexed
                                                }
                                            }
                                            when(filterType) {
                                                Constants.LEADS_FILTER_TYPE_DATE -> {
                                                    resetLeadsDateFilter()
                                                    if (LEADS_FILTER_TYPE_CUSTOM_DATE == item?.id) {
                                                        showDatePickerDialog()
                                                    } else {
                                                        val currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                                        var month = currentDate.get(Calendar.MONTH) + 1
                                                        var monthStr: String = if (month <= 9) "0$month" else "$month"
                                                        var day = currentDate.get(Calendar.DATE)
                                                        var dayStr: String = if (day <= 9) "0$day" else "$day"
                                                        mLeadsFilterEndDate = "${currentDate.get(Calendar.YEAR)}-$monthStr-$dayStr"
                                                        currentDate.add(Calendar.DAY_OF_YEAR, -abs(item?.id?.toInt() ?: 0))
                                                        month = currentDate.get(Calendar.MONTH) + 1
                                                        monthStr = if (month <= 9) "0$month" else "$month"
                                                        day = currentDate.get(Calendar.DATE)
                                                        dayStr = if (day <= 9) "0$day" else "$day"
                                                        mLeadsFilterStartDate = "${currentDate.get(Calendar.YEAR)}-$monthStr-$dayStr"
                                                        Log.d(TAG, "onLeadsFilterItemClickListener: startDate :: $mLeadsFilterStartDate endDate :: $mLeadsFilterEndDate")
                                                    }
                                                }
                                                Constants.LEADS_FILTER_TYPE_SORT -> leadsFilterSortType = item?.id?.toInt() ?: leadsFilterSortType
                                            }
                                            mLeadsFilterAdapter?.notifyItemChanged(recyclerRedrawPosition)
                                        }

                                    })
                                adapter = mLeadsFilterAdapter
                            }
                        }
                    }.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showFilterOptionsBottomSheet: ${e.message}", e)
            }
        }
    }

    private fun showDatePickerDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let { context ->
                SlyCalendarDialog()
                    .setEndDate(Date(MaterialDatePicker.todayInUtcMilliseconds()))
                    .setSelectedColor(context.getColor(R.color.black))
                    .setHeaderColor(context.getColor(R.color.black))
                    .setHeaderTextColor(context.getColor(R.color.white))
                    .setSelectedTextColor(context.getColor(R.color.white))
                    .setSingle(false)
                    .setFirstMonday(true)
                    .setCallback(object : SlyCalendarDialog.Callback {

                        override fun onCancelled() = resetLeadsDateFilter()

                        override fun onDataSelected(firstDate: Calendar?, secondDate: Calendar?, hours: Int, minutes: Int) {
                            Log.d(TAG, "onDataSelected: firstDate ${firstDate?.time} :: secondDate :: ${secondDate?.time}")
                            val secondDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            secondDateCalendar.timeInMillis = secondDate?.timeInMillis ?: firstDate?.timeInMillis ?: 0
                            var monthStr = "${(secondDateCalendar.get(Calendar.MONTH) + 1)}"
                            var dateStr = "${(secondDateCalendar.get(Calendar.DATE))}"
                            mLeadsFilterEndDate = "${secondDateCalendar.get(Calendar.YEAR)}-${if (1 == monthStr.length) "0$monthStr" else monthStr}-${if (1 == dateStr.length) "0$dateStr" else dateStr}"
                            val firstDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            firstDateCalendar.timeInMillis = firstDate?.timeInMillis ?: (secondDate?.timeInMillis ?: 0)
                            monthStr = "${(firstDateCalendar.get(Calendar.MONTH) + 1)}"
                            dateStr = "${(firstDateCalendar.get(Calendar.DATE))}"
                            mLeadsFilterStartDate = "${firstDateCalendar.get(Calendar.YEAR)}-${if (1 == monthStr.length) "0$monthStr" else monthStr}-${if (1 == dateStr.length) "0$dateStr" else dateStr}"
                            val displayDate = "${getDateStringFromLeadsFilter(firstDateCalendar.time)} - ${getDateStringFromLeadsFilter(secondDateCalendar.time)}"
                            mLeadsFilterList.forEachIndexed { pos, itemResponse ->
                                if (Constants.LEADS_FILTER_TYPE_DATE == itemResponse.type) {
                                    itemResponse.filterOptionsList.forEachIndexed { _, filterItemResponse ->
                                        if ("0" == filterItemResponse.id) {
                                            filterItemResponse.apply {
                                                isSelected = true
                                                customDateRangeStr = displayDate
                                            }
                                            mLeadsFilterAdapter?.notifyItemChanged(pos)
                                            return
                                        }
                                    }
                                    return@forEachIndexed
                                }
                            }
                        }
                    }).show(context.supportFragmentManager, TAG)
            }
        }
    }

    private fun resetLeadsDateFilter() {
        mLeadsFilterList.forEachIndexed { pos, itemResponse ->
            if (Constants.LEADS_FILTER_TYPE_DATE == itemResponse.type) {
                itemResponse.filterOptionsList.forEachIndexed { _, filterItemResponse ->
                    if (LEADS_FILTER_TYPE_CUSTOM_DATE == filterItemResponse.id) {
                        filterItemResponse.apply {
                            isSelected = false
                            customDateRangeStr = ""
                        }
                        mLeadsFilterAdapter?.notifyItemChanged(pos)
                        return
                    }
                }
                return@forEachIndexed
            }
        }
        mLeadsFilterEndDate = ""
        mLeadsFilterStartDate = ""
        mIsLeadsFilterReset = true
        val filterRedDotImageView: View? = mContentView?.findViewById(R.id.filterRedDotImageView)
        filterRedDotImageView?.visibility = View.GONE
    }

}