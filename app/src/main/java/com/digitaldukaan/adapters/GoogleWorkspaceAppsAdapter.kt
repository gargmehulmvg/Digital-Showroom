package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R

class GoogleWorkspaceAppsAdapter(
    private var mContext: Context?,
    private var mItemList: ArrayList<String>?
    ) : RecyclerView.Adapter<GoogleWorkspaceAppsAdapter.AddBankBottomSheetViewHolder>() {

    inner class AddBankBottomSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddBankBottomSheetViewHolder =
        AddBankBottomSheetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.google_work_space_item, parent, false))

    override fun getItemCount(): Int = mItemList?.size ?: 0

    override fun onBindViewHolder(holder: AddBankBottomSheetViewHolder, position: Int) {
        val item = mItemList?.get(position)
        mContext?.let { context ->
            Glide.with(context).load(item).into(holder.imageView)
        }
    }

}