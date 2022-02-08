package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.models.response.LeadsFilterListItemResponse

class LeadsFilterBottomSheetAdapter(
    private var mContext: Context?,
    private var mFilterList: ArrayList<LeadsFilterListItemResponse>?
    ) : RecyclerView.Adapter<LeadsFilterBottomSheetAdapter.LeadsFilterViewHolder>() {

    inner class LeadsFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)
        val separator: View = itemView.findViewById(R.id.separator)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeadsFilterViewHolder {
        return LeadsFilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bottom_sheet_leads_filter_item, parent, false))
    }

    override fun getItemCount(): Int = mFilterList?.size ?: 0

    override fun onBindViewHolder(holder: LeadsFilterViewHolder, position: Int) {
        holder.apply {
            val filterItem = mFilterList?.get(position)
            headingTextView.text = filterItem?.heading
            separator.visibility = if ((mFilterList?.size ?: 0) - 1 == position) View.INVISIBLE else View.VISIBLE
            recyclerView.apply {
                val gridLayoutManager = StaggeredGridLayoutManager(2, RecyclerView.HORIZONTAL)
                layoutManager = gridLayoutManager
                adapter = LeadsFilterOptionsItemAdapter(filterItem?.filterOptionsList, filterItem?.type)
            }
        }
    }
}