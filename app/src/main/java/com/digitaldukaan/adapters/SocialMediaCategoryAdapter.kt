package com.digitaldukaan.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.ISocialMediaTemplateItemClickListener
import com.digitaldukaan.models.response.SocialMediaCategoryItemResponse

class SocialMediaCategoryAdapter(
    private var mContext: Context?,
    private var mList: ArrayList<SocialMediaCategoryItemResponse?>?,
    private var mItemCount: Int,
    private var mListener: ISocialMediaTemplateItemClickListener?
) :
    RecyclerView.Adapter<SocialMediaCategoryAdapter.SocialMediaCategoryViewHolder>() {

    inner class SocialMediaCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val container: View = itemView.findViewById(R.id.container)
        val containerView: CardView = itemView.findViewById(R.id.containerView)
        val imageViewParent: CardView = itemView.findViewById(R.id.imageViewParent)
    }

    fun setListSize(count: Int) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            this.mItemCount = count
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialMediaCategoryViewHolder {
        val view = SocialMediaCategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.social_media_category_item_layout, parent, false)
        )
        view.container.setOnClickListener {
            mListener?.onSocialMediaTemplateCategoryItemClickListener(view.adapterPosition, mList?.get(view.adapterPosition))
        }
        return view
    }

    override fun getItemCount(): Int = if (0 == mItemCount) (mList?.size ?: 0) else mItemCount

    override fun onBindViewHolder(
        holder: SocialMediaCategoryViewHolder,
        position: Int
    ) {
        holder.apply {
            val item = mList?.get(position)
            textView.text = item?.text
            if (isNotEmpty(item?.textColor)) textView.setTextColor(Color.parseColor(item?.textColor))
            if (isNotEmpty(item?.textBgColor)) textView.setBackgroundColor(Color.parseColor(item?.textBgColor))
            mContext?.let { context -> Glide.with(context).load(item?.logo).into(imageView) }
            setHighlightedView(item?.isSelected ?: false, holder)
        }
    }

    private fun setHighlightedView(isSelected: Boolean, holder: SocialMediaCategoryViewHolder) {
        holder.apply {
            if (isSelected) {
                containerView.cardElevation = 15f
                mContext?.let { context ->
                    val param = imageViewParent.layoutParams as ViewGroup.MarginLayoutParams
                    param.setMargins(12,12,12,0)
                    imageViewParent.layoutParams = param
                    imageViewParent.radius = 25f
                    container.background = ContextCompat.getDrawable(context, R.drawable.curve_black_border_10)
                }
            } else {
                containerView.cardElevation = 1f
                val param = imageViewParent.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(0, 0, 0, 0)
                imageViewParent.layoutParams = param
                imageViewParent.radius = 1f
                container.background = null
            }
        }
    }

}