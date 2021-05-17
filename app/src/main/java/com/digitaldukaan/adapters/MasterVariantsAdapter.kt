package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.models.response.VariantItemResponse

class MasterVariantsAdapter(
    private var mContext: Context,
    private var mMasterVariantList: ArrayList<VariantItemResponse>?,
    private var mListener: IChipItemClickListener?
) : RecyclerView.Adapter<MasterVariantsAdapter.AddProductsChipsViewHolder>() {

    fun setMasterVariantList(list: ArrayList<VariantItemResponse>?) {
        mMasterVariantList = list
        notifyDataSetChanged()
    }

    inner class AddProductsChipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chipTextView: TextView = itemView.findViewById(R.id.chipTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddProductsChipsViewHolder {
        val holder = AddProductsChipsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_variant_chip_item, parent, false)
        )
        holder.chipTextView.setOnClickListener { mListener?.onChipItemClickListener(holder.adapterPosition) }
        return holder
    }

    override fun getItemCount(): Int = mMasterVariantList?.size ?: 0

    override fun onBindViewHolder(
        holder: AddProductsChipsViewHolder,
        position: Int
    ) {
        val item = mMasterVariantList?.get(position)
        holder.apply {
            chipTextView.text = item?.variantName
            if (item?.isSelected == true) {
                chipTextView.setTextColor(ContextCompat.getColor(mContext, R.color.default_text_light_grey))
                chipTextView.isEnabled = false
            } else {
                chipTextView.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                chipTextView.isEnabled = true
            }
        }
    }

}