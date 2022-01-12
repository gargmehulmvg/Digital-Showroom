package com.digitaldukaan.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IStoreSettingsItemClicked
import com.digitaldukaan.models.response.TrendingListResponse

class NewReleaseAdapter(
    private val newReleaseList: ArrayList<TrendingListResponse>?,
    private val listener: IStoreSettingsItemClicked,
    private val activity: MainActivity?,
    private val count: Int
) : RecyclerView.Adapter<NewReleaseAdapter.AppSettingsViewHolder>() {

    inner class AppSettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLayout: View = itemView.findViewById(R.id.itemLayout)
        val newReleaseLockGroup: View = itemView.findViewById(R.id.newReleaseLockGroup)
        val trendingNewTextView: TextView = itemView.findViewById(R.id.trendingNewTextView)
        val trendingTextView: TextView = itemView.findViewById(R.id.trendingTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val newTextView: ImageView = itemView.findViewById(R.id.newTextView)
        val backgroundImageView: ImageView = itemView.findViewById(R.id.backgroundImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppSettingsViewHolder {
        val view = AppSettingsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.new_release_item, parent, false))
        view.itemLayout.setOnClickListener{
            val releaseItem = newReleaseList?.get(view.adapterPosition)
            listener.onNewReleaseItemClicked(releaseItem)
        }
        return view
    }

    override fun getItemCount(): Int = count

    override fun onBindViewHolder(holder: NewReleaseAdapter.AppSettingsViewHolder, position: Int) {
        val responseItem = newReleaseList?.get(position)
        holder.run {
            textView.text = responseItem?.mText
            imageView.let {view -> activity?.let { context -> Glide.with(context).load(responseItem?.mCDN).into(view) } }
            if (isNotEmpty(responseItem?.mNewImageUrl)) {
                newTextView.visibility = View.VISIBLE
                newTextView.background = null
                activity?.let { context -> Glide.with(context).load(responseItem?.mNewImageUrl).into(newTextView) }
            } else newTextView.visibility = View.INVISIBLE
            activity?.let { context ->
                if (isNotEmpty(responseItem?.mTextColor)) {
                    textView.setTextColor(Color.parseColor(responseItem?.mTextColor))
                } else textView.setTextColor(Color.BLACK)
                if (isNotEmpty(responseItem?.mBgImage)) {
                    Glide.with(context).load(responseItem?.mBgImage).into(holder.backgroundImageView)
                } else {
                    backgroundImageView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }
            }
            when (responseItem?.mType) {
                Constants.NEW_RELEASE_TYPE_PREPAID_ORDER -> newTextView.visibility = View.VISIBLE
                Constants.NEW_RELEASE_TYPE_PAYMENT_MODES -> newTextView.visibility = View.VISIBLE
                Constants.NEW_RELEASE_TYPE_TRENDING -> {
                    trendingTextView.text = null
                    trendingNewTextView.apply {
                        visibility = View.VISIBLE
                        text = responseItem.mType
                    }
                }
                else -> {}
            }
            newReleaseLockGroup.visibility = if (true == responseItem?.isStaffFeatureLocked) View.VISIBLE else View.GONE
        }
    }

}