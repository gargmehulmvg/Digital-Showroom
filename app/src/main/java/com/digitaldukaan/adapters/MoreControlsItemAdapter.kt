package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.models.response.MoreControlsItemResponse
import com.digitaldukaan.models.response.MoreControlsStaticTextResponse
import com.digitaldukaan.services.serviceinterface.IMoreControlsItemClickListener

class MoreControlsItemAdapter(
    private var mContext: Context?,
    private var mStoreControlItemsList: ArrayList<MoreControlsItemResponse>?,
    private var mStoreControlStaticText: MoreControlsStaticTextResponse?,
    private var mListener: IMoreControlsItemClickListener
) : RecyclerView.Adapter<MoreControlsItemAdapter.MoreControlsItemViewHolder>() {

    inner class MoreControlsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.headingTextView)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreControlsItemViewHolder = MoreControlsItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.more_controls_item, parent, false))

    override fun getItemCount(): Int = mStoreControlItemsList?.size ?: 0

    override fun onBindViewHolder(holder: MoreControlsItemViewHolder, position: Int) {
        val item = mStoreControlItemsList?.get(position)
        holder.apply {
            textView.text = item?.heading
            recyclerView.apply {
                layoutManager = LinearLayoutManager(mContext)
                isNestedScrollingEnabled = false
                adapter = MoreControlsInnerItemAdapter(mContext, item?.children, mStoreControlStaticText, mListener)
            }
        }
    }

}