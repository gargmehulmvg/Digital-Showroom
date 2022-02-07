package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.ILeadsListItemListener
import com.digitaldukaan.models.response.LeadsResponse
import java.util.*

class LeadsAdapter(
    private var mContext: Context,
    private var mLeadsList: ArrayList<LeadsResponse>?,
    private var mListItemListener: ILeadsListItemListener?
) : RecyclerView.Adapter<LeadsAdapter.LeadsViewHolder>() {

    fun setLeadsList(leadsList: ArrayList<LeadsResponse>?) {
        this.mLeadsList = leadsList
        this.notifyDataSetChanged()
    }

    inner class LeadsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cartImageView: ImageView = itemView.findViewById(R.id.cartImageView)
        val leadItemContainer: View = itemView.findViewById(R.id.leadItemContainer)
        val leadDetailTextView: TextView = itemView.findViewById(R.id.leadDetailTextView)
        val leadLastUpdatedTextView: TextView = itemView.findViewById(R.id.leadLastUpdatedTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeadsViewHolder {
        val view = LeadsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.leads_item, parent, false))
        view.leadItemContainer.setOnClickListener {
            mListItemListener?.onLeadsItemCLickChanged(mLeadsList?.get(view.absoluteAdapterPosition))
        }
        return view
    }

    override fun getItemCount(): Int = mLeadsList?.size ?:0

    override fun onBindViewHolder(holder: LeadsAdapter.LeadsViewHolder, position: Int) {
        val item = mLeadsList?.get(position)
        holder.apply {
            val str = "${item?.phoneNumber} | ${item?.customerName}"
            leadDetailTextView.text = str
            priceTextView.text = "${item?.orderValue}"
            leadLastUpdatedTextView.text = item?.lastUpdateOn
            //Glide.with(mContext).load(item?.cartType).into(cartImageView)
        }
    }
}