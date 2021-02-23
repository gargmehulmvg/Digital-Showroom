package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager

class CommonWebViewFragment : BaseFragment() {

    private lateinit var mHeaderText: String

    fun newInstance(headerText: String): CommonWebViewFragment {
        val fragment = CommonWebViewFragment()
        fragment.mHeaderText = headerText
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.common_webview_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            onBackPressed(this@CommonWebViewFragment)
            setHeaderTitle(mHeaderText)
        }
    }
}