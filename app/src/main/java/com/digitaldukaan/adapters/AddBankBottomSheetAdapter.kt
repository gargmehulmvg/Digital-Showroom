package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R

class AddBankBottomSheetAdapter(
    private var mItemList: ArrayList<String>
) :
    RecyclerView.Adapter<AddBankBottomSheetAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.add_bank_bottom_sheet_item, parent, false))
    }

    override fun getItemCount(): Int = mItemList.size

    override fun onBindViewHolder(
        holder: ReferAndEarnViewHolder,
        position: Int
    ) {
        val item = mItemList.get(position)
        holder.apply {
            textView.text = item
        }
    }

}