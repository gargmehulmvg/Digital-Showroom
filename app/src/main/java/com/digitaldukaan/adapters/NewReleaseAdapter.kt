package com.digitaldukaan.adapters

import android.util.Log
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
import com.digitaldukaan.constants.isEmpty
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
        val trendingTextView: TextView = itemView.findViewById(R.id.trendingTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val newTextView: ImageView = itemView.findViewById(R.id.newTextView)
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
            imageView.let {view ->
                try {
                    activity?.let { context -> Glide.with(context).load(responseItem?.mCDN).into(view) }
                } catch (e: Exception) {
                    Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                }
            }
            if (!isEmpty(responseItem?.mNewImageUrl)) {
                newTextView.visibility = View.VISIBLE
                newTextView.background = null
                activity?.let { context -> Glide.with(context).load(responseItem?.mNewImageUrl).into(newTextView) }
            }
            when (responseItem?.mType) {
                Constants.NEW_RELEASE_TYPE_GOOGLE_ADS -> activity?.let {
                    itemLayout.background = ContextCompat.getDrawable(it, R.drawable.slight_curve_google_ads_background)
                    textView.setTextColor(ContextCompat.getColor(it, R.color.white))
                }
                Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> activity?.let { itemLayout.background = ContextCompat.getDrawable(it, R.drawable.curve_premium_selector) }
                Constants.NEW_RELEASE_TYPE_PREPAID_ORDER -> newTextView.visibility = View.VISIBLE
                Constants.NEW_RELEASE_TYPE_PREMIUM -> activity?.let { itemLayout.background = ContextCompat.getDrawable(it, R.drawable.curve_premium_selector) }
                Constants.NEW_RELEASE_TYPE_NEW -> newTextView.visibility = View.VISIBLE
                Constants.NEW_RELEASE_TYPE_PAYMENT_MODES -> newTextView.visibility = View.VISIBLE
                Constants.NEW_RELEASE_TYPE_TRENDING -> {
                    newTextView.visibility = View.GONE
                    trendingTextView.text = responseItem.mType
                    trendingTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_trending, 0, 0, 0)
                }
                else -> {}
            }
        }
    }

}