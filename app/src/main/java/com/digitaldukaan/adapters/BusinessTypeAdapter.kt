package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.adapters.BusinessTypeAdapter.BusinessTypeViewHolder
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.models.response.BusinessTypeItemResponse

class BusinessTypeAdapter(
    private val mContext: BaseFragment,
    private var mBusinessList: ArrayList<BusinessTypeItemResponse>) :
    RecyclerView.Adapter<BusinessTypeViewHolder>() {

    inner class BusinessTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val businessTypeCheckBox: CheckBox = itemView.findViewById(R.id.businessTypeCheckBox)
        val businessTypeImageView: ImageView = itemView.findViewById(R.id.businessTypeImageView)
        val businessTypeContainer: View = itemView.findViewById(R.id.businessTypeContainer)
        val businessTypeTextView: TextView = itemView.findViewById(R.id.businessTypeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessTypeViewHolder {
        val view = BusinessTypeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.business_type_item, parent, false))
        view.businessTypeContainer.setOnClickListener {
            if (it.isSelected) {
                mBusinessList[view.absoluteAdapterPosition].isBusinessTypeSelected = false
                it.isSelected = false
                view.businessTypeCheckBox.isChecked = false
            } else {
                var count = 0
                mBusinessList.forEachIndexed { _, itemResponse -> if (itemResponse.isBusinessTypeSelected) ++count }
                if (count >= mContext.resources.getInteger(R.integer.business_type_count)) {
                    mContext.showToast("Only ${mContext.resources.getInteger(R.integer.business_type_count)} selections are allowed")
                    return@setOnClickListener
                }
                mBusinessList[view.absoluteAdapterPosition].isBusinessTypeSelected = true
                it.isSelected = true
                view.businessTypeCheckBox.isChecked = true
            }
        }
        return view
    }

    override fun getItemCount(): Int = mBusinessList.size

    override fun onBindViewHolder(
        holder: BusinessTypeViewHolder,
        position: Int
    ) {
        val itemResponse = mBusinessList[position]
        holder.apply {
            if (itemResponse.isBusinessTypeSelected) {
                businessTypeContainer.isSelected = true
                businessTypeCheckBox.isChecked = true
            }
            businessTypeTextView.text = itemResponse.businessName
            if (isNotEmpty(itemResponse.businessImage)) Glide.with(mContext).load(itemResponse.businessImage).into(businessTypeImageView)
        }
    }

}