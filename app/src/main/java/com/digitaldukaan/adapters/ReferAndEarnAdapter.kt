package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.models.response.WorkJourneyItemResponse

class ReferAndEarnAdapter(
    private var mJourneyList: ArrayList<WorkJourneyItemResponse>?
) :
    RecyclerView.Adapter<ReferAndEarnAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val referAndEarnAmountTextView: TextView = itemView.findViewById(R.id.referAndEarnAmountTextView)
        val referAndEarnTextView: TextView = itemView.findViewById(R.id.referAndEarnTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.refer_and_earn_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mJourneyList?.size ?: 0

    override fun onBindViewHolder(
        holder: ReferAndEarnViewHolder,
        position: Int
    ) {
        val item = mJourneyList?.get(position)
        holder.apply {
            referAndEarnTextView.text = item?.title
            referAndEarnAmountTextView.text = "₹${item?.amount}"
        }
    }

}