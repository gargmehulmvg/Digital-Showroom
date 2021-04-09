package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.interfaces.IStoreSettingsItemClicked
import com.digitaldukaan.models.response.TrendingListResponse
import com.squareup.picasso.Picasso

class NewReleaseAdapter(
    private val newReleaseList: ArrayList<TrendingListResponse>?,
    private val listener: IStoreSettingsItemClicked
) : RecyclerView.Adapter<NewReleaseAdapter.AppSettingsViewHolder>() {

    inner class AppSettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLayout: View = itemView.findViewById(R.id.itemLayout)
        val trendingTextView: TextView = itemView.findViewById(R.id.trendingTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val newTextView: View = itemView.findViewById(R.id.newTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppSettingsViewHolder {
        val view = AppSettingsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.new_release_item, parent, false))
        view.itemLayout.setOnClickListener{listener.onNewReleaseItemClicked(newReleaseList?.get(view.adapterPosition))}
        return view
    }

    override fun getItemCount(): Int = newReleaseList?.size ?: 0


    override fun onBindViewHolder(holder: NewReleaseAdapter.AppSettingsViewHolder, position: Int) {
        val responseItem = newReleaseList?.get(position)
        holder.run {
            textView.text = responseItem?.mText
            Picasso.get().load(responseItem?.mCDN).into(imageView)
            when (responseItem?.mType) {
                Constants.NEW_RELEASE_TYPE_NEW -> newTextView.visibility = View.VISIBLE
                Constants.NEW_RELEASE_TYPE_TRENDING -> {
                    newTextView.visibility = View.GONE
                    trendingTextView.text = responseItem.mType
                    trendingTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_trending,
                        0,
                        0,
                        0
                    )
                }
            }
        }
    }

}