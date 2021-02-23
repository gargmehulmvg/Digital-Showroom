package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R

class ProfileStatusAdapter(private val mTotalSteps: Int?, private val mCompletedSteps: Int?) :
    RecyclerView.Adapter<ProfileStatusAdapter.ProfileStatusViewHolder>() {

    inner class ProfileStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mProfileStatusItemView: TextView = itemView.findViewById(R.id.profileStatusItemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileStatusViewHolder {
        return ProfileStatusViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.profile_status_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mTotalSteps ?: 0

    override fun onBindViewHolder(
        holder: ProfileStatusAdapter.ProfileStatusViewHolder,
        position: Int
    ) {
        mCompletedSteps?.let {
            holder.mProfileStatusItemView.isEnabled = ((position + 1) <= mCompletedSteps)
        }
    }

}