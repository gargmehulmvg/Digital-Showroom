package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R

class ProfileStatusAdapter(private val mTotalSteps: Int?, private val mCompletedSteps: Int?, private val mContext: Context) :
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
            if (mTotalSteps?.minus(mCompletedSteps) == 1) {
                holder.mProfileStatusItemView.background = ContextCompat.getDrawable(mContext, R.drawable.profile_status_selector_green)
            } else holder.mProfileStatusItemView.background = ContextCompat.getDrawable(mContext, R.drawable.profile_status_selector_yellow)
            holder.mProfileStatusItemView.isEnabled = ((position + 1) <= mCompletedSteps)
        }
    }

}