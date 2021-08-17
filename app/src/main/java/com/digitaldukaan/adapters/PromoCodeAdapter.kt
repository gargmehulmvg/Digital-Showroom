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
import com.digitaldukaan.interfaces.IPromoCodeItemClickListener
import com.digitaldukaan.models.response.PromoCodeListItemResponse
import com.digitaldukaan.models.response.PromoCodePageStaticTextResponse

class PromoCodeAdapter(
    private var mStaticText: PromoCodePageStaticTextResponse? = null,
    private var mContext: Context?,
    private var mPromoCodeList: ArrayList<PromoCodeListItemResponse>?,
    private var mListener: IPromoCodeItemClickListener?
) : RecyclerView.Adapter<PromoCodeAdapter.PromoCodeViewHolder>() {

    private var mPromoCodeMode: String = ""

    inner class PromoCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clickEvent: View = itemView.findViewById(R.id.clickEvent)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val visibleStatusTextView: TextView = itemView.findViewById(R.id.visibleStatusTextView)
        val useCodeTextView: TextView = itemView.findViewById(R.id.useCodeTextView)
        val detailsTextView: TextView = itemView.findViewById(R.id.detailsTextView)
        val shareCouponsTextView: TextView = itemView.findViewById(R.id.shareCouponsTextView)
    }

    fun setList(list: ArrayList<PromoCodeListItemResponse>?, mode: String) {
        this.mPromoCodeList = list
        this.mPromoCodeMode = mode
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoCodeViewHolder {
        val view = PromoCodeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_promo_code_item, parent, false))
        view.detailsTextView.setOnClickListener { mListener?.onPromoCodeDetailClickListener(view.adapterPosition) }
        view.clickEvent.setOnClickListener { mListener?.onPromoCodeDetailClickListener(view.adapterPosition) }
        view.shareCouponsTextView.setOnClickListener { mListener?.onPromoCodeShareClickListener(view.adapterPosition) }
        return view
    }

    override fun getItemCount(): Int = mPromoCodeList?.size ?: 0

    override fun onBindViewHolder(holder: PromoCodeViewHolder, position: Int) {
        val item = mPromoCodeList?.get(position)
        holder.apply {
            val descriptionStr = if (Constants.MODE_COUPON_TYPE_FLAT == item?.discountType) {
                "${mStaticText?.text_flat} ₹${item.discount?.toInt()} ${mStaticText?.text_off_all_caps}"
            } else {
                "${item?.discount?.toInt()}% ${mStaticText?.text_off_all_caps} ${mStaticText?.text_upto_capital} ₹${item?.maxDiscount?.toInt()}"
            }
            if (Constants.MODE_INACTIVE == mPromoCodeMode) {
                shareCouponsTextView.apply {
                    alpha = 0.2f
                    isEnabled = false
                }
            } else {
                shareCouponsTextView.apply {
                    alpha = 1f
                    isEnabled = true
                }
            }
            shareCouponsTextView.text = mStaticText?.text_share_coupon
            detailsTextView.text = mStaticText?.text_details
            descriptionTextView.text = descriptionStr
            val codeStr = "${mStaticText?.text_use_code} ${item?.promoCode}"
            useCodeTextView.text = codeStr
            mContext?.let { context ->
                when {
                    Constants.MODE_INACTIVE == mPromoCodeMode -> {
                        visibleStatusTextView.apply {
                            text = mStaticText?.text_inactive_coupon
                            background = ContextCompat.getDrawable(context, R.drawable.slight_curve_black_dotted_border_grey_background)
                            setTextColor(ContextCompat.getColor(context, R.color.dark_blackish))
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                        }
                    }
                    true == item?.isWebsiteVisible -> {
                        visibleStatusTextView.apply {
                            text = mStaticText?.text_visible_on_website
                            background = ContextCompat.getDrawable(context, R.drawable.slight_curve_green_dotted_border_green_background)
                            setTextColor(ContextCompat.getColor(context, R.color.open_green))
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_eye_on, 0, 0, 0)
                        }
                    }
                    else -> {
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

}