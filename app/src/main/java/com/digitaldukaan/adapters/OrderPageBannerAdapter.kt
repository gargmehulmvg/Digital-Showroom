package com.digitaldukaan.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.HomePageBannerResponse

class OrderPageBannerAdapter(
    private val mContext: BaseFragment?,
    private val bannerList: ArrayList<HomePageBannerResponse>?,
    private val listener: IAdapterItemClickListener
) : RecyclerView.Adapter<OrderPageBannerAdapter.MarketingCardViewHolder>() {

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doubleSpanImageView: ImageView = itemView.findViewById(R.id.doubleSpanImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        val view = MarketingCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.order_page_banner_item, parent, false))
        view.doubleSpanImageView.setOnClickListener { listener.onAdapterItemClickListener(view.adapterPosition) }
        return view
    }

    override fun getItemCount(): Int = bannerList?.size ?: 0

    override fun onBindViewHolder(
        holder: MarketingCardViewHolder,
        position: Int
    ) {
       holder.run {
           doubleSpanImageView?.let {
               try {
                   mContext?.let { context -> Glide.with(context).load(bannerList?.get(position)?.mBannerUrl).into(it) }
               } catch (e: Exception) {
                   Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
               }
           }
       }
    }

}