package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R

class PaymentOffersBottomSheetAdapter(
    private var mItemList: ArrayList<String>
    ) : RecyclerView.Adapter<PaymentOffersBottomSheetAdapter.PaymentOffersBottomSheetViewHolder>() {

    inner class PaymentOffersBottomSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView? = itemView.findViewById(R.id.activeOffersTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentOffersBottomSheetViewHolder =
        PaymentOffersBottomSheetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bank_offer_sheet_item, parent, false))

    override fun getItemCount(): Int = mItemList.size

    override fun onBindViewHolder(holder: PaymentOffersBottomSheetViewHolder, position: Int) {
        val item = mItemList[position]
        holder.textView?.text = item
    }

}