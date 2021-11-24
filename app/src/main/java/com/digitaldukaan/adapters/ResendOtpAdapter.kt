package com.digitaldukaan.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IResendOtpItemClickListener
import com.digitaldukaan.models.response.CommonCtaResponse

class ResendOtpAdapter(
    private var mContext: Context?,
    private var mList: ArrayList<CommonCtaResponse?>?,
    private var mItemCount: Int,
    private var mListener: IResendOtpItemClickListener?
) :
    RecyclerView.Adapter<ResendOtpAdapter.ResendOtpViewHolder>() {

    inner class ResendOtpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.resendOtpTextView)
        val imageView: ImageView = itemView.findViewById(R.id.resendOtpItemImageView)
        val container: View = itemView.findViewById(R.id.resendOtpItemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResendOtpViewHolder {
        val view = ResendOtpViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.resend_otp_item_layout, parent, false))
        view.container.setOnClickListener {
            mListener?.onResendOtpItemClickListener(view.adapterPosition, mList?.get(view.adapterPosition))
        }
        return view
    }

    override fun getItemCount(): Int = if (0 == mItemCount) (mList?.size ?: 0) else mItemCount

    override fun onBindViewHolder(holder: ResendOtpViewHolder, position: Int) {
        holder.apply {
            val item = mList?.get(position)

            textView.text = item?.text
            if (isNotEmpty(item?.textColor)) textView.setTextColor(Color.parseColor(item?.textColor))

            if (isNotEmpty(item?.cdn)) mContext?.let { context -> Glide.with(context).load(item?.cdn).into(imageView) }
        }
    }

}