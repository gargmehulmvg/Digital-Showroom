package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.interfaces.IAppSettingsItemClicked
import com.digitaldukaan.models.response.SubPagesResponse
import com.squareup.picasso.Picasso

class AppSettingsAdapter(private val mAppSettingsItemClicked: IAppSettingsItemClicked) : RecyclerView.Adapter<AppSettingsAdapter.AppSettingsViewHolder>() {

    private var mAppSettingsList: ArrayList<SubPagesResponse> = ArrayList()

    fun setAppSettingsList(list: ArrayList<SubPagesResponse>?) {
        list?.let {
            CoroutineScopeUtils().runTaskOnCoroutineMain {
                mAppSettingsList = list
                notifyDataSetChanged()
            }
        }
    }

    inner class AppSettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mAppSettingTextView: TextView = itemView.findViewById(R.id.appSettingTextView)
        val mAppSettingImageView: ImageView = itemView.findViewById(R.id.appSettingImageView)
        val mAppSettingLayout: View = itemView.findViewById(R.id.appSettingLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppSettingsViewHolder {
        val view = AppSettingsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.app_setting_item, parent, false))
        view.mAppSettingLayout.setOnClickListener{mAppSettingsItemClicked.onAppSettingItemClicked(mAppSettingsList[view.adapterPosition])}
        return view
    }

    override fun getItemCount(): Int = mAppSettingsList.size


    override fun onBindViewHolder(holder: AppSettingsAdapter.AppSettingsViewHolder, position: Int) {
        val response = mAppSettingsList[position]
        holder.mAppSettingTextView.text = response.mText
        Picasso.get().load(response.mLogo).into(holder.mAppSettingImageView)
    }

}