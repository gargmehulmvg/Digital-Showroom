package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IStoreSettingsItemClicked
import com.digitaldukaan.models.response.StoreOptionsResponse

class SettingsStoreAdapter(
    private val mContext: Context?,
    private val listener: IStoreSettingsItemClicked
) :
    RecyclerView.Adapter<SettingsStoreAdapter.AppSettingsViewHolder>() {

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
        val settingArrowImageView: ImageView = itemView.findViewById(R.id.settingArrowImageView)
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
            settingImageView.let {
                mContext?.let { context -> Glide.with(context).load(response.mLogo).into(it) }
            }
            if (isNotEmpty(response.mBannerText)) {
                settingSubTitleTextView.visibility = View.VISIBLE
                settingSubTitleTextView.text = response.mBannerText
            }
            mContext?.let { context ->
                when {
                    response.mIsStaffFeatureLocked -> {
                        settingArrowImageView.apply {
                            visibility = View.VISIBLE
                            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_subscription_locked_black_small))
                        }
                    }
                    response.mIsShowMore -> {
                        settingArrowImageView.apply {
                            visibility = View.VISIBLE
                            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_half_arrow_forward))
                        }
                    }
                    else -> settingArrowImageView.visibility = View.GONE
                }
            }
        }
    }

}