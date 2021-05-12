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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ActiveVariantAdapter
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IVariantItemClickListener
import com.digitaldukaan.models.response.VariantItemResponse
import kotlinx.android.synthetic.main.layout_add_variant.*
import java.util.*


class AddVariantFragment: BaseFragment() {

    private var mVariantsList: ArrayList<VariantItemResponse>? = null
    private var variantNameEditText: EditText? = null
    private var activeVariantRecyclerView: RecyclerView? = null
    private var mActiveVariantAdapter:ActiveVariantAdapter? = null

    companion object {
        fun newInstance(variantsList: ArrayList<VariantItemResponse>?): AddVariantFragment{
            val fragment = AddVariantFragment()
            fragment.mVariantsList = variantsList
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
        variantNameEditText = mContentView.findViewById(R.id.variantNameEditText)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mActiveVariantAdapter = ActiveVariantAdapter(mActivity, mVariantsList, object : IVariantItemClickListener {
            override fun onVariantItemClickListener(position: Int) {

            }

            override fun onVariantEditNameClicked(variant: VariantItemResponse?) {
                showToast(variant?.variantName)
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
    }

    private fun showDeleteVariantConfirmationDialog(position: Int) {
        val dialog = Dialog(mActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_delete_variant_confirmation)
        val deleteVariantMessageTextView = dialog.findViewById(R.id.deleteVariantMessageTextView) as TextView
        val deleteVariantTextView: View = dialog.findViewById(R.id.deleteVariantTextView)
        val deleteVariantCancelTextView: View = dialog.findViewById(R.id.deleteVariantCancelTextView)
        deleteVariantTextView.setOnClickListener {
            dialog.dismiss()
            mActiveVariantAdapter?.deleteItemFromActiveVariantList(position)
        }
        deleteVariantCancelTextView.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backButtonToolbar?.id -> mActivity.onBackPressed()
            addTextView?.id -> addVariantToActiveVariantList()
        }
    }

    private fun addVariantToActiveVariantList() {
        val variantName = variantNameEditText?.text?.toString()
        if (variantName?.isEmpty() == true) {
            variantNameEditText?.apply {
                error = getString(R.string.mandatory_field_message)
                requestFocus()
            }
            return
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
            return
        }
        val variant = VariantItemResponse(0, 1, 1, variantName)
        mVariantsList?.add(variant)
        mActiveVariantAdapter?.setActiveVariantList(mVariantsList)
        variantNameEditText?.text = null
    }

}