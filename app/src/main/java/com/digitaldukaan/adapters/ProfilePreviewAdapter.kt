package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IProfilePreviewItemClicked
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse

class ProfilePreviewAdapter(
    private var mSettingsKeysList: ArrayList<ProfilePreviewSettingsKeyResponse>,
    private val mProfilePreviewListener: IProfilePreviewItemClicked
) :
    RecyclerView.Adapter<ProfilePreviewAdapter.ProfilePreviewViewHolder>() {

    inner class ProfilePreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingKeyHeading: TextView = itemView.findViewById(R.id.settingKeyHeading)
        val addSettingKeyHeading: TextView = itemView.findViewById(R.id.addSettingKeyHeading)
        val profilePreviewContainer: View = itemView.findViewById(R.id.profilePreviewContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePreviewViewHolder {
        val view = ProfilePreviewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.profile_preview_item, parent, false))
        view.profilePreviewContainer.setOnClickListener{ mProfilePreviewListener.onProfilePreviewItemClicked(mSettingsKeysList[view.adapterPosition], view.adapterPosition + 1) }
        return view
    }

    override fun getItemCount(): Int = mSettingsKeysList.size

    override fun onBindViewHolder(
        holder: ProfilePreviewAdapter.ProfilePreviewViewHolder,
        position: Int
    ) {
        val settingKeyItem = mSettingsKeysList[position]
        settingKeyItem.run {
            holder.settingKeyHeading.text = mHeadingText
            holder.addSettingKeyHeading.text = mDefaultText
        }
    }

}