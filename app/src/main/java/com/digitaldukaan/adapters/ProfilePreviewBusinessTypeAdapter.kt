package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.models.response.StoreBusinessResponse
import com.squareup.picasso.Picasso

class ProfilePreviewBusinessTypeAdapter(
    private val mBusinessList: ArrayList<StoreBusinessResponse>?
) :
    RecyclerView.Adapter<ProfilePreviewBusinessTypeAdapter.AppSettingsViewHolder>() {

    inner class AppSettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppSettingsViewHolder {
        return AppSettingsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_preview_business_type_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mBusinessList?.size ?: 0

    override fun onBindViewHolder(
        holder: ProfilePreviewBusinessTypeAdapter.AppSettingsViewHolder,
        position: Int
    ) {
        holder.apply {
            imageView?.let { Picasso.get().load(mBusinessList?.get(position)?.image).into(it) }
        }
    }

}