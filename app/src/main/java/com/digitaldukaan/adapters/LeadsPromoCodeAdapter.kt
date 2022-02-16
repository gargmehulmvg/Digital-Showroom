package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.interfaces.IPromoCodeItemClickListener
import com.digitaldukaan.models.response.LeadDetailStaticTextResponse
import com.digitaldukaan.models.response.PromoCodeListItemResponse

class LeadsPromoCodeAdapter(
    private var mStaticText: LeadDetailStaticTextResponse? = null,
    private var mPromoCodeList: ArrayList<PromoCodeListItemResponse>?,
    private var mListener: IPromoCodeItemClickListener?
) : RecyclerView.Adapter<LeadsPromoCodeAdapter.PromoCodeViewHolder>() {

    fun setList(list: ArrayList<PromoCodeListItemResponse>?) {
        this.mPromoCodeList = list
        notifyDataSetChanged()
    }

    inner class PromoCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.container)
        val offerTextView: TextView = itemView.findViewById(R.id.offerTextView)
        val useCodeTextView: TextView = itemView.findViewById(R.id.useCodeTextView)
        val useCodeValueTextView: TextView = itemView.findViewById(R.id.useCodeValueTextView)
        val shareTextView: TextView = itemView.findViewById(R.id.shareTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoCodeViewHolder {
        val view = PromoCodeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_leads_promo_code_item, parent, false))
        view.container.setOnClickListener { mListener?.onPromoCodeShareClickListener(view.adapterPosition) }
        return view
    }

    override fun getItemCount(): Int = mPromoCodeList?.size ?: 0

    override fun onBindViewHolder(holder: PromoCodeViewHolder, position: Int) {
        val item = mPromoCodeList?.get(position)
        holder.apply {
            offerTextView.text = if (Constants.MODE_COUPON_TYPE_FLAT == item?.discountType) {
                "${mStaticText?.textFlat} ₹${item.discount?.toInt()} ${mStaticText?.textOff}"
            } else {
                "${item?.discount?.toInt()}% ${mStaticText?.textOff} Upto ₹${item?.maxDiscount?.toInt()}"
            }
            useCodeTextView.text = "${mStaticText?.textUseCode}:"
            shareTextView.text = mStaticText?.textShare
            useCodeValueTextView.text = item?.promoCode
        }
    }

}