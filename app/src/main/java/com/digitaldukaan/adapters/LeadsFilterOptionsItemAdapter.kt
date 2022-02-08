package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.models.response.LeadsFilterOptionsItemResponse

class LeadsFilterOptionsItemAdapter(
    private var mFilterItemList: ArrayList<LeadsFilterOptionsItemResponse>?,
    private var mFilterType: String?
    ) : RecyclerView.Adapter<LeadsFilterOptionsItemAdapter.LeadsFilterViewHolder>() {

    inner class LeadsFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.itemTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeadsFilterViewHolder {
        return LeadsFilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bototm_sheet_filter_options_item, parent, false))
    }

    override fun getItemCount(): Int = mFilterItemList?.size ?: 0

    override fun onBindViewHolder(holder: LeadsFilterViewHolder, position: Int) {
        holder.apply {
            textView.text = mFilterItemList?.get(position)?.text
        }
    }

}