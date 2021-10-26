package com.digitaldukaan.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.TemplateBackgroundItemResponse

class TemplateBackgroundAdapter(
    private val mContext: Context?,
    private var mColorList: ArrayList<TemplateBackgroundItemResponse>?,
    private var mListener: IAdapterItemClickListener?
) :
    RecyclerView.Adapter<TemplateBackgroundAdapter.MarketingCardViewHolder>() {

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemContainer: View = itemView.findViewById(R.id.itemContainer)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val tickImageView: ImageView = itemView.findViewById(R.id.tickImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        val view = MarketingCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_template_background_item, parent, false))
        view.itemContainer.setOnClickListener {
            mListener?.onAdapterItemClickListener(view.adapterPosition)
        }
        return view
    }

    override fun getItemCount(): Int = mColorList?.size ?: 0

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    override fun onBindViewHolder(holder: MarketingCardViewHolder, position: Int) {
        val item = mColorList?.get(position)
        holder.run {
            if (Constants.CTA_TYPE_SOLID == item?.type) {
                imageView.setBackgroundColor(Color.parseColor(item.name))
            } else {
                mContext?.let { context -> Glide.with(context).load(item?.name).into(imageView) }
            }
            tickImageView.visibility = if (true == item?.isSelected) View.VISIBLE else View.GONE
        }
    }

}