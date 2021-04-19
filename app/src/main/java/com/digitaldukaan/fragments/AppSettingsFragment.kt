package com.digitaldukaan.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.AppSettingsAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IAppSettingsItemClicked
import com.digitaldukaan.models.response.AccountStaticTextResponse
import com.digitaldukaan.models.response.SubPagesResponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.layout_app_setting_fragment.*

class AppSettingsFragment : BaseFragment(), IAppSettingsItemClicked {

    private var mAppSettingsList: ArrayList<SubPagesResponse>? = ArrayList()
    private var mHeaderText: String? = null
    private lateinit var mAppSettingsResponseStaticData: AccountStaticTextResponse

    fun newInstance(list: ArrayList<SubPagesResponse>?, headerText: String?, appSettingsResponseStaticData: AccountStaticTextResponse): AppSettingsFragment {
        val fragment = AppSettingsFragment()
        fragment.mAppSettingsList = list
        fragment.mHeaderText = headerText
        fragment.mAppSettingsResponseStaticData = appSettingsResponseStaticData
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_app_setting_fragment, container, false)

        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = AppSettingsAdapter(this)
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
        hideBottomNavigationView(true)
        adapter.setAppSettingsList(mAppSettingsList)
        appVersionTextView.text = "${mAppSettingsResponseStaticData.mAppVersionText} v.${BuildConfig.VERSION_CODE}"
        storeIdTextView.text = "${mAppSettingsResponseStaticData.mStoreId} ${getStringDataFromSharedPref(Constants.STORE_ID)}"
    }

    private fun showLogoutDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
            builder.apply {
                setTitle(mAppSettingsResponseStaticData.mLogoutTitle)
                setMessage(mAppSettingsResponseStaticData.mLogoutBody)
                setCancelable(false)
                setPositiveButton(mAppSettingsResponseStaticData.mLogoutText) { dialog, _ ->
                    mActivity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
                    dialog.dismiss()
                    clearFragmentBackStack()
                    storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, "")
                    storeStringDataInSharedPref(Constants.STORE_NAME, "")
                    storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, "")
                    storeStringDataInSharedPref(Constants.STORE_ID, "")
                    launchFragment(LoginFragment.newInstance(), false)
                }
                setNegativeButton(getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
    }

    override fun onAppSettingItemClicked(subPagesResponse: SubPagesResponse) {
        if (Constants.ACTION_LOGOUT == subPagesResponse.mAction) showLogoutDialog() else openUrlInBrowser(subPagesResponse.mPage)
    }

    private fun showAddBankBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_bank_account,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
            }
        }.show()
    }

}