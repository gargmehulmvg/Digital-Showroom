package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R

class PaymentModeAdapter(
    private var mContext: Context?
) :
    RecyclerView.Adapter<PaymentModeAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentModeChildRecyclerView: RecyclerView? = itemView.findViewById(R.id.paymentModeChildRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_payment_mode_item, parent, false)
        )
    }

    override fun getItemCount(): Int = 2

    override fun onBindViewHolder(
        holder: ReferAndEarnViewHolder,
        position: Int
    ) {
        holder.apply {
            paymentModeChildRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mContext)
                adapter = PaymentModeChildAdapter(mContext)
            }
        }
    }

}