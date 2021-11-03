package com.digitaldukaan.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.LandingPageMessageListItemResponse

class LandingPageAddProductsAdapter(
    private val mContext: Context?,
    private var mList: ArrayList<LandingPageMessageListItemResponse>?,
    private var mListener: IAdapterItemClickListener?
) :
    RecyclerView.Adapter<LandingPageAddProductsAdapter.LandingPageAddProductsViewHolder>() {

    inner class LandingPageAddProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LandingPageAddProductsViewHolder {
        val view = LandingPageAddProductsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.landing_page_add_products_item, parent, false))
        view.container.setOnClickListener {
            val position = view.adapterPosition
            if (position < 0 || position > (mList?.size ?: 0)) return@setOnClickListener
            if (true == mList?.get(position)?.isProductAdded) return@setOnClickListener
            mListener?.onAdapterItemClickListener(position)
        }
        return view
    }

    override fun getItemCount(): Int = mList?.size ?: 0

    override fun onBindViewHolder(holder: LandingPageAddProductsViewHolder, position: Int) {
        val item = mList?.get(position)
        holder.apply {
            mContext?.let { context ->
                if (true == item?.isProductAdded && isNotEmpty(item.imageUrl)) {
                    Glide.with(context).load(item.imageUrl).into(imageView)
                } else if (true == item?.isProductAdded && isEmpty(item.imageUrl)) {
                    container.setBackgroundColor(Color.WHITE)
                    Glide.with(context).load(R.drawable.ic_tick_without_shadow).into(imageView2)
                } else {
                    container.background = ContextCompat.getDrawable(context, R.drawable.ic_dotted_white_transparent_bg)
                }
            }
        }
    }

}