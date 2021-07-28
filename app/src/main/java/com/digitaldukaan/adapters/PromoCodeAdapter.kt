package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.PromoCodeListItemResponse
import com.digitaldukaan.models.response.PromoCodePageStaticTextResponse

class PromoCodeAdapter(
    private var mStaticText: PromoCodePageStaticTextResponse? = null,
    private var mContext: Context?,
    private var mPromoCodeList: ArrayList<PromoCodeListItemResponse>?,
    private var mListener: IAdapterItemClickListener?
) : RecyclerView.Adapter<PromoCodeAdapter.PromoCodeViewHolder>() {

    inner class PromoCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val visibleStatusTextView: TextView = itemView.findViewById(R.id.visibleStatusTextView)
        val useCodeTextView: TextView = itemView.findViewById(R.id.useCodeTextView)
        val detailsTextView: TextView = itemView.findViewById(R.id.detailsTextView)
        val shareCouponsTextView: TextView = itemView.findViewById(R.id.shareCouponsTextView)
    }

    fun setList(list: ArrayList<PromoCodeListItemResponse>?) {
        this.mPromoCodeList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoCodeViewHolder {
        val view = PromoCodeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_promo_code_item, parent, false)
        )
        view.detailsTextView.setOnClickListener {
            mListener?.onAdapterItemClickListener(view.adapterPosition)
        }
        return view
    }

    override fun getItemCount(): Int = mPromoCodeList?.size ?: 0

    override fun onBindViewHolder(
        holder: PromoCodeViewHolder,
        position: Int
    ) {
        val item = mPromoCodeList?.get(position)
        holder.apply {
            val descriptionStr = if (Constants.MODE_COUPON_TYPE_FLAT == item?.discountType) {
                "${mStaticText?.text_flat} ₹${item.discount?.toInt()} ${mStaticText?.text_off_all_caps}"
            } else {
                "${item?.discount?.toInt()}% ${mStaticText?.text_off_all_caps} ${mStaticText?.text_upto_capital} ₹${item?.maxDiscount?.toInt()}"
            }
            shareCouponsTextView.text = mStaticText?.text_share_coupon
            detailsTextView.text = mStaticText?.text_details
            descriptionTextView.text = descriptionStr
            val codeStr = "${mStaticText?.text_use_code} ${item?.promoCode}"
            useCodeTextView.text = codeStr
            mContext?.let { context ->
                if (Constants.MODE_PROMO_CODE_ACTIVE == item?.status) {
                    visibleStatusTextView.apply {
                        text = mStaticText?.text_visible_on_website
                        background = ContextCompat.getDrawable(context, R.drawable.slight_curve_green_dotted_border_green_background)
                        setTextColor(ContextCompat.getColor(context, R.color.open_green))
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_eye_on, 0, 0, 0)
                    }
                } else {
                    visibleStatusTextView.apply {
                        text = mStaticText?.text_hidden_from_website
                        background = ContextCompat.getDrawable(context, R.drawable.slight_curve_red_dotted_border_green_background)
                        setTextColor(ContextCompat.getColor(context, R.color.red_switch_off))
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_eye_off, 0, 0, 0)
                    }
                }
            }
        }
    }

}