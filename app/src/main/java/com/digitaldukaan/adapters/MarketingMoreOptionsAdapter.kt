package com.digitaldukaan.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IMarketingMoreOptionsItemClicked
import com.digitaldukaan.models.response.MarketingMoreOptionsItemResponse
import com.squareup.picasso.Picasso

class MarketingMoreOptionsAdapter(
    private val mMarketingMoreOptionsItemClicked: IMarketingMoreOptionsItemClicked,
    private var mOptionsList: ArrayList<MarketingMoreOptionsItemResponse?>?
) :
    RecyclerView.Adapter<MarketingMoreOptionsAdapter.AppSettingsViewHolder>() {

    inner class AppSettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mAppSettingTextView: TextView = itemView.findViewById(R.id.appSettingTextView)
        val mAppSettingImageView: ImageView = itemView.findViewById(R.id.appSettingImageView)
        val mSeparator: View = itemView.findViewById(R.id.separator)
        val mAppSettingLayout: View = itemView.findViewById(R.id.appSettingLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppSettingsViewHolder {
        val view = AppSettingsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.app_setting_item, parent, false))
        view.mAppSettingLayout.setOnClickListener {
            mMarketingMoreOptionsItemClicked.onMarketingMoreOptionsItemClicked(mOptionsList?.get(view.adapterPosition))
        }
        return view
    }

    override fun getItemCount(): Int = mOptionsList?.size ?: 0

    override fun onBindViewHolder(holder: MarketingMoreOptionsAdapter.AppSettingsViewHolder, position: Int) {
        val response = mOptionsList?.get(position)
        holder.mSeparator.visibility = if ((mOptionsList?.size ?: 0) - 1 == position) View.GONE else View.VISIBLE
        holder.mAppSettingTextView.text = response?.heading
        holder.mAppSettingImageView.let {
            try {
                Picasso.get().load(response?.leftCdn).into(it)
            } catch (e: Exception) {
                Log.e(MarketingMoreOptionsAdapter::class.java.simpleName, "picasso image loading issue: ${e.message}", e)
            }
        }
    }

}