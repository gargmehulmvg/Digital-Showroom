package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.AppSettingsAdapter
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.SubPagesResponse
import kotlinx.android.synthetic.main.app_setting_fragment.*

class AppSettingsFragment : BaseFragment() {

    private var mAppSettingsList: ArrayList<SubPagesResponse>? = ArrayList()
    private var mHeaderText: String? = null
    private val mAppSettingsStaticData = mStaticData.mStaticData.mSettingsStaticData

    fun newInstance(list: ArrayList<SubPagesResponse>?, headerText: String?): AppSettingsFragment {
        val fragment = AppSettingsFragment()
        fragment.mAppSettingsList = list
        fragment.mHeaderText = headerText
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.app_setting_fragment, container, false)

        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = AppSettingsAdapter()
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            onBackPressed(this@AppSettingsFragment)
            setHeaderTitle(mHeaderText)
        }
        appSettingsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(mActivity)
            setAdapter(adapter)
        }
        adapter.setAppSettingsList(mAppSettingsList)
        appVersionTextView.text = "${mAppSettingsStaticData.mAppVersionText} v.${BuildConfig.VERSION_CODE}"
        storeIdTextView.text = "${mAppSettingsStaticData.mStoreId} 2018"
    }

}