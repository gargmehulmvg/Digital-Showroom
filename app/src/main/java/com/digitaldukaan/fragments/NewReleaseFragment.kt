package com.digitaldukaan.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.NewReleaseAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IStoreSettingsItemClicked
import com.digitaldukaan.models.response.AccountStaticTextResponse
import com.digitaldukaan.models.response.StoreOptionsResponse
import com.digitaldukaan.models.response.TrendingListResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class NewReleaseFragment: BaseFragment(), IStoreSettingsItemClicked {

    private var mNewReleaseList: ArrayList<TrendingListResponse>? = null
    private var mHeadingStr: String? = null
    private var mSettingsStaticData: AccountStaticTextResponse? = null

    companion object {
        private const val TAG = "NewReleaseFragment"
        fun newInstance(trendingList: ArrayList<TrendingListResponse>?, staticTextResponse: AccountStaticTextResponse?): NewReleaseFragment {
            val fragment = NewReleaseFragment()
            fragment.mNewReleaseList = trendingList
            fragment.mSettingsStaticData = staticTextResponse
            fragment.mHeadingStr = staticTextResponse?.heading_new_releases
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_new_release_fragment, container, false)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle(mHeadingStr)
            setSideIconVisibility(false)
            setSecondSideIconVisibility(false)
        }
        val newReleaseRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.newReleaseRecyclerView)
        newReleaseRecyclerView?.apply {
            layoutManager = GridLayoutManager(mActivity, 3)
            adapter = NewReleaseAdapter(mNewReleaseList, this@NewReleaseFragment, mActivity, mNewReleaseList?.size ?: 0)
        }
        hideBottomNavigationView(true)
        return mContentView
    }

    override fun onStoreSettingItemClicked(subPagesResponse: StoreOptionsResponse) {
        Log.d(TAG, "onStoreSettingItemClicked: do nothing")
    }

    override fun onNewReleaseItemClicked(responseItem: TrendingListResponse?) {
        when (responseItem?.mAction) {
            Constants.NEW_RELEASE_TYPE_WEBVIEW -> {
                val eventName = when (responseItem.mType) {
                    Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> AFInAppEventType.EVENT_GET_CUSTOM_DOMAIN
                    Constants.NEW_RELEASE_TYPE_PREMIUM -> AFInAppEventType.EVENT_PREMIUM_PAGE
                    else -> AFInAppEventType.EVENT_VIEW_TOP_STORES
                }
                AppEventsManager.pushAppEvents(
                    eventName = eventName,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(
                            Constants.STORE_ID),
                        AFInAppEventParameterName.CHANNEL to "Settings Page"
                    )
                )
                openWebViewFragment(this, "", BuildConfig.WEB_VIEW_URL + responseItem.mPage)
            }
            Constants.NEW_RELEASE_TYPE_EXTERNAL -> {
                val eventName = when (responseItem.mType) {
                    Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> AFInAppEventType.EVENT_GET_CUSTOM_DOMAIN
                    Constants.NEW_RELEASE_TYPE_PREMIUM -> AFInAppEventType.EVENT_PREMIUM_PAGE
                    else -> AFInAppEventType.EVENT_VIEW_TOP_STORES
                }
                AppEventsManager.pushAppEvents(
                    eventName = eventName,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(
                            Constants.STORE_ID),
                        AFInAppEventParameterName.CHANNEL to "Settings Page"
                    )
                )
                when (responseItem.mType) {
                    Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> {
                        openUrlInBrowser(responseItem.mPage + PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                    }
                    Constants.NEW_RELEASE_TYPE_PREPAID_ORDER -> launchFragment(SetOrderTypeFragment.newInstance(), true)
                    else -> openUrlInBrowser(responseItem.mPage)
                }

            }
            else -> showTrendingOffersBottomSheet()
        }
    }

    private fun showTrendingOffersBottomSheet() {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(
                R.layout.bottom_sheet_trending_offers,
                it.findViewById(R.id.bottomSheetContainer)
            )
            bottomSheetDialog.apply {
                setContentView(view)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                view?.run {
                    val bottomSheetBrowserText: TextView = findViewById(R.id.bottomSheetBrowserText)
                    val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                    val bottomSheetUrl: TextView = findViewById(R.id.bottomSheetUrl)
                    val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                    bottomSheetBrowserText.text = mSettingsStaticData?.mBestViewedText
                    val txtSpannable = SpannableString(Constants.DOTPE_OFFICIAL_URL)
                    val boldSpan = StyleSpan(Typeface.BOLD)
                    txtSpannable.setSpan(boldSpan, 6, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    bottomSheetUrl.text = txtSpannable
                    bottomSheetHeadingTextView.setHtmlData(mSettingsStaticData?.mBottomSheetText)
                    bottomSheetUrl.setOnClickListener { copyDataToClipboard(Constants.DOTPE_OFFICIAL_URL_CLIPBOARD) }
                    bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                }
            }.show()
        }
    }

}