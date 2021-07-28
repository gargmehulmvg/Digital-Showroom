package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IVariantItemClickListener
import com.digitaldukaan.models.response.PromoCodeListItemResponse

class PromoCodeAdapter(
    private var mContext: Context?,
    private var mPromoCodeList: ArrayList<PromoCodeListItemResponse>?,
    private var mListener: IVariantItemClickListener?
) :
    RecyclerView.Adapter<PromoCodeAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val inStockTextView: TextView = itemView.findViewById(R.id.inStockTextView)
//        val variantNameTextView: TextView = itemView.findViewById(R.id.variantNameTextView)
//        val optionsMenuImageView: View = itemView.findViewById(R.id.optionsMenuImageView)
//        val variantSwitch: SwitchMaterial = itemView.findViewById(R.id.variantSwitch)
//        val variantView: ConstraintLayout = itemView.findViewById(R.id.constraintLayout8)
    }

    fun setList(list: ArrayList<PromoCodeListItemResponse>?) {
        this.mPromoCodeList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_promo_code_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mPromoCodeList?.size ?: 0

    override fun onBindViewHolder(
        holder: ReferAndEarnViewHolder,
        position: Int
    ) {
        val item = mPromoCodeList?.get(position)
        holder.apply {

        }
    }

}