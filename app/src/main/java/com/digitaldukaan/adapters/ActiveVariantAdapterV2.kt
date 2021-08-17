package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IVariantItemClickListener
import com.digitaldukaan.models.response.AddProductStaticText
import com.digitaldukaan.models.response.VariantItemResponse
import com.google.android.material.textfield.TextInputLayout

class ActiveVariantAdapterV2(
    private var mContext: Context?,
    private var mStaticText: AddProductStaticText?,
    private var mActiveVariantList: ArrayList<VariantItemResponse>?,
    private var mListener: IVariantItemClickListener?
) :
    RecyclerView.Adapter<ActiveVariantAdapterV2.ActiveVariantViewHolder>() {

    inner class ActiveVariantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val variantNameInputLayout: TextInputLayout = itemView.findViewById(R.id.variantNameInputLayout)
        val variantPriceInputLayout: TextInputLayout = itemView.findViewById(R.id.variantPriceInputLayout)
        val deleteTextView: TextView = itemView.findViewById(R.id.deleteTextView)
        val variantDiscountPriceInputLayout: TextInputLayout = itemView.findViewById(R.id.variantDiscountPriceInputLayout)
    }

    fun setActiveVariantList(activeVariantList: ArrayList<VariantItemResponse>?) {
        this.mActiveVariantList = activeVariantList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveVariantViewHolder {
        return ActiveVariantViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_active_variant_item_v2, parent, false)
        )
    }

    //override fun getItemCount(): Int = mActiveVariantList?.size ?: 2
    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(
        holder: ActiveVariantViewHolder,
        position: Int
    ) {
        val item = mActiveVariantList?.get(position)
        holder.apply {
            deleteTextView.text = mStaticText?.text_delete
            variantNameInputLayout.hint = mStaticText?.hint_variant_name
            variantPriceInputLayout.hint = mStaticText?.hint_price
            variantDiscountPriceInputLayout.hint = mStaticText?.hint_discounted_price
        }
    }

    fun deleteItemFromActiveVariantList(position: Int) {
        mActiveVariantList?.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mActiveVariantList?.size ?: 0)
    }

}