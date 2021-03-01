package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.BusinessTypeAdapter.BusinessTypeViewHolder
import com.digitaldukaan.models.response.BusinessTypeItemResponse
import com.squareup.picasso.Picasso

class BusinessTypeAdapter(
    private val mContext: Context,
    private var mBusinessList: ArrayList<BusinessTypeItemResponse>
) :
    RecyclerView.Adapter<BusinessTypeViewHolder>() {

    inner class BusinessTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val businessTypeCheckBox: CheckBox = itemView.findViewById(R.id.businessTypeCheckBox)
        val businessTypeImageView: ImageView = itemView.findViewById(R.id.businessTypeImageView)
        val businessTypeContainer: View = itemView.findViewById(R.id.businessTypeContainer)
        val businessTypeTextView: TextView = itemView.findViewById(R.id.businessTypeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessTypeViewHolder {
        val view = BusinessTypeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.business_type_item, parent, false)
        )
        view.businessTypeContainer.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(mContext, R.anim.zoom_out)
            it.startAnimation(animation)
            if (it.isSelected) {
                it.isSelected = false
                view.businessTypeCheckBox.isChecked = false
            } else {
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
            businessTypeTextView.text = itemResponse.businessName
            if (itemResponse.businessImage.isNotEmpty()) Picasso.get().load(itemResponse.businessImage).into(businessTypeImageView)
        }
    }

}