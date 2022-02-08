package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.models.response.LeadsFilterResponse

class LeadsFilterOptionsBottomSheetAdapter(private var mItemList: ArrayList<LeadsFilterResponse>?) :
    RecyclerView.Adapter<LeadsFilterOptionsBottomSheetAdapter.LeadsFilterOptionsViewHolder>() {

    inner class LeadsFilterOptionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTextView: TextView? = itemView.findViewById(R.id.itemTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeadsFilterOptionsViewHolder{
        val view = LeadsFilterOptionsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.leads_item, parent, false))
        return view
    }

    override fun getItemCount(): Int = mItemList?.size ?: 0

    override fun onBindViewHolder(holder: LeadsFilterOptionsViewHolder, position: Int) {
        holder.itemTextView?.text = "Last 7 days"
    }

}