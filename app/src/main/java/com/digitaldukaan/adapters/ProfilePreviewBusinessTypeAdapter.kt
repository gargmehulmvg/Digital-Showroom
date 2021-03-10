package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R

class ProfilePreviewBusinessTypeAdapter(private val activity: MainActivity) :
    RecyclerView.Adapter<ProfilePreviewBusinessTypeAdapter.AppSettingsViewHolder>() {

    inner class AppSettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mAppSettingTextView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppSettingsViewHolder {
        val view = AppSettingsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.profile_preview_business_type_item, parent, false)
        )
        return view
    }

    override fun getItemCount(): Int = 10


    override fun onBindViewHolder(holder: ProfilePreviewBusinessTypeAdapter.AppSettingsViewHolder, position: Int) {
        holder.mAppSettingTextView.setImageDrawable(
            ContextCompat.getDrawable(
                activity,
                if (position % 5 == 0) R.drawable.ic_auto_data_backup else if (position % 3 == 0) R.drawable.ic_whatsapp else R.drawable.ic_delivery_on
            )
        )
    }

}