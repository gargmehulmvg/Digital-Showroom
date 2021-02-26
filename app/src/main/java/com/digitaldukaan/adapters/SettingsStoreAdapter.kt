package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.interfaces.IStoreSettingsItemClicked
import com.digitaldukaan.models.response.StoreOptionsResponse
import com.squareup.picasso.Picasso

class SettingsStoreAdapter(private val listener: IStoreSettingsItemClicked) : RecyclerView.Adapter<SettingsStoreAdapter.AppSettingsViewHolder>() {

    private var mStoreOptionsList: ArrayList<StoreOptionsResponse> = ArrayList()

    fun setSettingsList(list: ArrayList<StoreOptionsResponse>?) {
        list?.let {
            CoroutineScopeUtils().runTaskOnCoroutineMain {
                mStoreOptionsList = list
                notifyDataSetChanged()
            }
        }
    }

    inner class AppSettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingSubTitleTextView: TextView = itemView.findViewById(R.id.settingSubTitleTextView)
        val settingTitleTextView: TextView = itemView.findViewById(R.id.settingTitleTextView)
        val settingImageView: ImageView = itemView.findViewById(R.id.settingImageView)
        val settingArrowImageView: View = itemView.findViewById(R.id.settingArrowImageView)
        val appSettingLayout: View = itemView.findViewById(R.id.appSettingLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppSettingsViewHolder {
        val view = AppSettingsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.setting_store_item, parent, false))
        view.appSettingLayout.setOnClickListener{listener.onStoreSettingItemClicked(mStoreOptionsList[view.adapterPosition])}
        return view
    }

    override fun getItemCount(): Int = mStoreOptionsList.size


    override fun onBindViewHolder(holder: SettingsStoreAdapter.AppSettingsViewHolder, position: Int) {
        val response = mStoreOptionsList[position]
        holder.run {
            settingTitleTextView.text = response.mText
            Picasso.get().load(response.mLogo).into(settingImageView)
            if (response.mBannerText?.isEmpty() == false) {
                settingSubTitleTextView.visibility = View.VISIBLE
                settingSubTitleTextView.text = response.mBannerText
            }
            if (response.mIsShowMore) settingArrowImageView.visibility = View.VISIBLE
        }
    }

}