package com.digitaldukaan.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.models.response.HowItWorksItemResponse
import com.squareup.picasso.Picasso
import io.sentry.Sentry

class PrepaidOrderWorkFlowAdapter(
    private var mHowItGoesList: ArrayList<HowItWorksItemResponse>?
) :
    RecyclerView.Adapter<PrepaidOrderWorkFlowAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val countTextView: TextView = itemView.findViewById(R.id.countTextView)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.prepaid_order_work_flow_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mHowItGoesList?.size ?: 0

    override fun onBindViewHolder(
        holder: ReferAndEarnViewHolder,
        position: Int
    ) {
        holder.apply {
            val item = mHowItGoesList?.get(position)
            textView.text = item?.text
            val countStr = "0${position + 1}"
            countTextView.text = countStr
            if (!isEmpty(item?.url)) {
                try {
                    Picasso.get().load(item?.url).into(imageView)
                } catch (e: Exception) {
                    Log.e("PrepaidOrderWorkFlowAdapter", "onBindViewHolder: ", e)
                    Sentry.captureException(e, "PrepaidOrderWorkFlowAdapter :: onBindViewHolder")
                }
            }
        }
    }

}