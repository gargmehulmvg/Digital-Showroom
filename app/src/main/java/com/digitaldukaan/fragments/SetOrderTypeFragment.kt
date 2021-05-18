package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.SetOrderTypeAdapter
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.AccountStaticTextResponse

class SetOrderTypeFragment: BaseFragment() {

    private var mMoreControlsStaticData: AccountStaticTextResponse? = null

    companion object {
        fun newInstance(moreControlsStaticData: AccountStaticTextResponse?): SetOrderTypeFragment{
            val fragment = SetOrderTypeFragment()
            fragment.mMoreControlsStaticData = moreControlsStaticData
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_set_order_type_fragment, container, false)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("Set Orders Type")
            setSideIconVisibility(false)
            setSecondSideIconVisibility(false)
        }
        hideBottomNavigationView(true)
        setupUI()
        return mContentView
    }

    private fun setupUI() {
        val prepaidOrderTypeRecyclerView: RecyclerView? = mContentView.findViewById(R.id.prepaidOrderTypeRecyclerView)
        prepaidOrderTypeRecyclerView?.apply {
            layoutManager = LinearLayoutManager(mActivity)
            adapter = SetOrderTypeAdapter(mActivity)
        }
        val bothOrderTypeRecyclerView: RecyclerView? = mContentView.findViewById(R.id.bothOrderTypeRecyclerView)
        bothOrderTypeRecyclerView?.apply {
            layoutManager = LinearLayoutManager(mActivity)
            adapter = SetOrderTypeAdapter(mActivity)
        }
    }

}