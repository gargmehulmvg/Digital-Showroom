package com.digitaldukaan.adapters

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IProductItemClickListener
import com.digitaldukaan.models.response.ProductResponse

class ProductsAdapter(
    private val mContext: Context?,
    private val mProductsList: ArrayList<ProductResponse>?,
    private val mListener: IProductItemClickListener) : RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>() {

    inner class ProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.container)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val originalPriceTextView: TextView = itemView.findViewById(R.id.originalPriceTextView)
        val discountedPriceTextView: TextView = itemView.findViewById(R.id.discountedPriceTextView)
        val promoCodeTextView: TextView = itemView.findViewById(R.id.promoCodeTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        val view = ProductsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.products_item, parent, false))
        view.container.setOnClickListener {
            mListener.onProductItemClickListener(mProductsList?.get(view.adapterPosition))
        }
        return view
    }

    override fun getItemCount(): Int = mProductsList?.size ?: 0

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val item = mProductsList?.get(position)
        holder.apply {
            titleTextView.text = item?.name
            var discountPriceStr: String? = ""
            discountPriceStr = if (item?.price != item?.discountedPrice) {
                "???${item?.discountedPrice}"
            } else {
                null
            }
            if (isEmpty(discountPriceStr)) {
                discountedPriceTextView.visibility = View.GONE
            } else {
                discountedPriceTextView.visibility = View.VISIBLE
                discountedPriceTextView.text = discountPriceStr
            }
            originalPriceTextView.apply {
                val priceStr= "???${item?.price}"
                text = priceStr
                if (isNotEmpty(discountPriceStr)) paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            if (isNotEmpty(item?.imageUrl)) mContext?.let { context -> Glide.with(context).load(item?.imageUrl).into(imageView) }
        }
    }

}