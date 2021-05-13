package com.digitaldukaan.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ActiveVariantAdapter
import com.digitaldukaan.adapters.MasterVariantsAdapter
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.interfaces.IVariantItemClickListener
import com.digitaldukaan.models.response.AddProductResponse
import com.digitaldukaan.models.response.VariantItemResponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.layout_add_variant.*
import java.util.*
import kotlin.collections.ArrayList


class AddVariantFragment: BaseFragment(), IChipItemClickListener {

    private var mVariantsList: ArrayList<VariantItemResponse>? = null
    private var mRecentVariantsList: ArrayList<VariantItemResponse>? = null
    private var mMasterVariantsList: ArrayList<VariantItemResponse>? = null
    private var appSubTitleTextView: TextView? = null
    private var variantNameEditText: EditText? = null
    private var activeVariantRecyclerView: RecyclerView? = null
    private var masterVariantRecyclerView: RecyclerView? = null
    private var recentVariantRecyclerView: RecyclerView? = null
    private var mActiveVariantAdapter: ActiveVariantAdapter? = null
    private var mMasterVariantsAdapter: MasterVariantsAdapter? = null
    private var mRecentVariantsAdapter: MasterVariantsAdapter? = null
    private var mProductName: String? = ""
    private var mAddProductResponse: AddProductResponse? = null

    companion object {
        fun newInstance(addProductResponse: AddProductResponse?): AddVariantFragment{
            val fragment = AddVariantFragment()
            fragment.mAddProductResponse = addProductResponse
            val variantsList = addProductResponse?.storeItem?.variantsList
            if (!isEmpty(variantsList)) {
                fragment.mVariantsList = ArrayList()
                variantsList?.forEachIndexed { _, itemResponse ->
                    val variant = VariantItemResponse(
                        itemResponse.variantId,
                        itemResponse.status,
                        itemResponse.available,
                        itemResponse.variantName,
                        itemResponse.masterId,
                        itemResponse.isSelected
                    )
                    fragment.mVariantsList?.add(variant)
                }
            } else fragment.mVariantsList = variantsList
            fragment.mRecentVariantsList = addProductResponse?.recentVariantsList
            fragment.mMasterVariantsList = addProductResponse?.masterVariantsList
            fragment.mProductName = addProductResponse?.storeItem?.name
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_add_variant, container, false)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, true)
        }
        activeVariantRecyclerView = mContentView.findViewById(R.id.activeVariantRecyclerView)
        masterVariantRecyclerView = mContentView.findViewById(R.id.masterVariantRecyclerView)
        recentVariantRecyclerView = mContentView.findViewById(R.id.recentVariantRecyclerView)
        variantNameEditText = mContentView.findViewById(R.id.variantNameEditText)
        appSubTitleTextView = mContentView.findViewById(R.id.appSubTitleTextView)
        if (isEmpty(mProductName)) {
            appSubTitleTextView?.visibility = View.GONE
        } else appSubTitleTextView?.text = mProductName
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (isEmpty(mVariantsList)) mVariantsList = ArrayList()
        refreshAllVariantsList()
        mActiveVariantAdapter = ActiveVariantAdapter(mActivity, mVariantsList, object : IVariantItemClickListener {
            override fun onVariantItemClickListener(position: Int) {

            }

            override fun onVariantEditNameClicked(variant: VariantItemResponse?, position: Int) {
                showEditVariantNameBottomSheet(variant, position)
            }

            override fun onVariantDeleteClicked(position: Int) {
                showDeleteVariantConfirmationDialog(position)
            }
        })
        activeVariantRecyclerView?.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(mActivity)
            adapter = mActiveVariantAdapter
        }
        mMasterVariantsAdapter = MasterVariantsAdapter(mActivity, mMasterVariantsList, this@AddVariantFragment)
        masterVariantRecyclerView?.apply {
            layoutManager = GridLayoutManager(mActivity, 3)
            adapter = mMasterVariantsAdapter
        }
        mRecentVariantsAdapter = MasterVariantsAdapter(mActivity, mRecentVariantsList, object : IChipItemClickListener {
            override fun onChipItemClickListener(position: Int) {
                val recentVariant = mRecentVariantsList?.get(position)
                val isVariantNameAlreadyExist = isVariantNameAlreadyExist(recentVariant?.variantName, null)
                if (isVariantNameAlreadyExist) return
                val variant = VariantItemResponse(0, 1, 1, recentVariant?.variantName, 0, false)
                mVariantsList?.add(variant)
                mActiveVariantAdapter?.setActiveVariantList(mVariantsList)
                recentVariant?.isSelected = true
                mRecentVariantsAdapter?.setMasterVariantList(mRecentVariantsList)

            }
        })
        recentVariantRecyclerView?.apply {
            layoutManager = LinearLayoutManager(mActivity)
            adapter = mRecentVariantsAdapter
        }
    }

    private fun refreshAllVariantsList() {
        mMasterVariantsList?.forEachIndexed { _, itemResponse -> itemResponse.isSelected = false }
        mRecentVariantsList?.forEachIndexed { _, itemResponse -> itemResponse.isSelected = false }
        mVariantsList?.let {
            for (item in it) {
                mMasterVariantsList?.forEachIndexed { _, itemResponse ->
                    if (item.variantName == itemResponse.variantName) {
                        itemResponse.isSelected = true
                        return@forEachIndexed
                    }
                }
            }
            for (item in it) {
                mRecentVariantsList?.forEachIndexed { _, itemResponse ->
                    if (item.variantName == itemResponse.variantName) {
                        itemResponse.isSelected = true
                        return@forEachIndexed
                    }
                }
            }
        }
    }

    private fun showDeleteVariantConfirmationDialog(position: Int) {
        val dialog = Dialog(mActivity)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.dialog_delete_variant_confirmation)
            val deleteVariantMessageTextView: TextView = dialog.findViewById(R.id.deleteVariantMessageTextView)
            val deleteVariantTextView: View = dialog.findViewById(R.id.deleteVariantTextView)
            val deleteVariantCancelTextView: View = dialog.findViewById(R.id.deleteVariantCancelTextView)
            deleteVariantTextView.setOnClickListener {
                dialog.dismiss()
                mActiveVariantAdapter?.deleteItemFromActiveVariantList(position)
                refreshAllVariantsList()
                mRecentVariantsAdapter?.notifyDataSetChanged()
                mMasterVariantsAdapter?.notifyDataSetChanged()
            }
            deleteVariantCancelTextView.setOnClickListener { dialog.dismiss() }
        }.show()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backButtonToolbar?.id -> mActivity.onBackPressed()
            addTextView?.id -> addVariantToActiveVariantList()
            saveTextView?.id -> saveVariantList()
        }
    }

    private fun saveVariantList() {
        if (isEmpty(mVariantsList)) {
            showToast("Please add at least 1 variant")
        } else {
            mAddProductResponse?.storeItem?.variantsList = mVariantsList
            mActivity.onBackPressed()
        }
    }

    private fun addVariantToActiveVariantList() {
        val variantName = variantNameEditText?.text?.toString()
        if (isVariantNameAlreadyExist(variantName, variantNameEditText)) return
        val variant = VariantItemResponse(0, 1, 1, variantName, 0, false)
        mVariantsList?.add(variant)
        mActiveVariantAdapter?.setActiveVariantList(mVariantsList)
        variantNameEditText?.text = null
    }

    private fun isVariantNameAlreadyExist(variantName: String?, variantNameEditText: EditText?): Boolean {
        if (isEmpty(variantName)) {
            variantNameEditText?.apply {
                error = getString(R.string.mandatory_field_message)
                requestFocus()
            }
            return true
        }
        var isVariantNameExist = false
        mVariantsList?.forEachIndexed { _, itemResponse ->
            if (itemResponse.variantName?.toLowerCase(Locale.getDefault())?.trim() == variantName?.toLowerCase(Locale.getDefault())?.trim()) {
                isVariantNameExist = true
                return@forEachIndexed
            }
        }
        if (isVariantNameExist) {
            variantNameEditText?.apply {
                error = "Unique variant name is required"
                requestFocus()
            }
            return true
        }
        return false
    }

    private fun showEditVariantNameBottomSheet(variant: VariantItemResponse?, position: Int) {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_edit_variant_name,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val saveTextView: TextView = findViewById(R.id.saveTextView)
                val variantNameEditText: EditText = findViewById(R.id.variantNameEditText)
                variantNameEditText.setText(variant?.variantName)
                saveTextView.setOnClickListener {
                    val variantName = variantNameEditText.text.toString().trim()
                    if (isVariantNameAlreadyExist(variantName, variantNameEditText)) return@setOnClickListener
                    mVariantsList?.get(position)?.variantName = variantName
                    mActiveVariantAdapter?.notifyDataSetChanged()
                    refreshAllVariantsList()
                    mRecentVariantsAdapter?.notifyDataSetChanged()
                    mMasterVariantsAdapter?.notifyDataSetChanged()
                    bottomSheetDialog.dismiss()
                }
            }
        }.show()
    }

    override fun onChipItemClickListener(position: Int) {
        val masterVariant = mMasterVariantsList?.get(position)
        val isVariantNameAlreadyExist = isVariantNameAlreadyExist(masterVariant?.variantName, null)
        if (isVariantNameAlreadyExist) return
        val variant = VariantItemResponse(0, 1, 1, masterVariant?.variantName, masterVariant?.variantId, false)
        mVariantsList?.add(variant)
        mActiveVariantAdapter?.setActiveVariantList(mVariantsList)
        masterVariant?.isSelected = true
        mMasterVariantsAdapter?.setMasterVariantList(mMasterVariantsList)
    }

}