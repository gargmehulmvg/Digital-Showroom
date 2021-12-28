package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.models.response.BillingPosFeatureListItemResponse

class MainFeaturesPosAdapter(
    private var mItemList: ArrayList<BillingPosFeatureListItemResponse?>?,
    private var mContext: MainActivity?
    ) : RecyclerView.Adapter<MainFeaturesPosAdapter.MainFeaturesPosViewHolder>() {

    inner class MainFeaturesPosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainFeaturesPosViewHolder =
        MainFeaturesPosViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.main_feature_pos_item, parent, false))

    override fun getItemCount(): Int = mItemList?.size ?: 0

    override fun onBindViewHolder(holder: MainFeaturesPosViewHolder, position: Int) {
        val item = mItemList?.get(position)
        holder.textView.text =  item?.text
        if (isNotEmpty(item?.url)) mContext?.let { context -> Glide.with(context).load(item?.url).into(holder.imageView) }
    }

}