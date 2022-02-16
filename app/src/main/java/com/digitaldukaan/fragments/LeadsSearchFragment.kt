package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.LeadsAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.ILeadsListItemListener
import com.digitaldukaan.models.request.LeadsListRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.LeadsResponse
import com.digitaldukaan.services.LeadsSearchService
import com.digitaldukaan.services.serviceinterface.ILeadsSearchServiceInterface
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.activity_main2.backButtonToolbar
import kotlinx.android.synthetic.main.layout_leads_search.*

class LeadsSearchFragment: BaseFragment(), ILeadsSearchServiceInterface, ILeadsListItemListener {

    private var mService: LeadsSearchService? = null
    private var recyclerView:RecyclerView? = null
    private var mLeadsAdapter: LeadsAdapter? = null

    companion object {
        fun newInstance(): LeadsSearchFragment = LeadsSearchFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "LeadsSearchFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_leads_search, container, false)
        mService = LeadsSearchService()
        mService?.setServiceListener(this)
        recyclerView = mContentView?.findViewById(R.id.recyclerView)
        recyclerView?.apply {
            isNestedScrollingEnabled = true
            mLeadsAdapter = LeadsAdapter(context, ArrayList(), this@LeadsSearchFragment)
            layoutManager = LinearLayoutManager(mActivity)
            adapter = mLeadsAdapter
            recyclerView?.addItemDecoration(StickyRecyclerHeadersDecoration(mLeadsAdapter)) //9653340191
        }
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        hideBottomNavigationView(true)
        searchEditText?.apply {
            requestFocus()
            showKeyboard()
            addTextChangedListener(object : TextWatcher {

                private var mIsInputNumberIsNumeric: Boolean = false

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                override fun afterTextChanged(s: Editable?) {
                    val str = s?.toString()
                    if (1 == str?.length) mIsInputNumberIsNumeric = isNumeric(str)
                    if (isNotEmpty(str)) {
                        val leadsFilterRequest = LeadsListRequest(
                            userName = if (!mIsInputNumberIsNumeric) str else "",
                            startDate = "",
                            endDate = "",
                            userPhone = if (mIsInputNumberIsNumeric) str else "",
                            sortType = Constants.SORT_TYPE_DESCENDING,
                            cartType = Constants.CART_TYPE_DEFAULT
                        )
                        mService?.getCartsByFilters(leadsFilterRequest)
                    } else {
                        noLeadsLayout?.visibility = View.GONE
                        recyclerView?.visibility = View.GONE
                    }
                }

            })
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            backButtonToolbar?.id -> mActivity?.onBackPressed()
        }
    }

    override fun onLeadsSearchException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onGetCartsByFiltersResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val listType = object : TypeToken<ArrayList<LeadsResponse>>() {}.type
                val leadsList: ArrayList<LeadsResponse> = Gson().fromJson(commonResponse.mCommonDataStr, listType)
                if (isEmpty(leadsList)) {
                    noLeadsLayout?.visibility = View.VISIBLE
                    recyclerView?.visibility = View.GONE
                } else {
                    noLeadsLayout?.visibility = View.GONE
                    recyclerView?.visibility = View.VISIBLE
                    leadsList.forEachIndexed { _, itemResponse -> itemResponse.updatedDate = getDateFromOrderString(itemResponse.lastUpdateOn) }
                }
                mLeadsAdapter?.setLeadsList(leadsList)
            }
        }
    }

    override fun onLeadsItemCLickedListener(item: LeadsResponse?) {
        launchFragment(LeadDetailFragment.newInstance(item), true)
    }

}