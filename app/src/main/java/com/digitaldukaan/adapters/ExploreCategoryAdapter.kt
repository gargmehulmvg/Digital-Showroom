package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.models.response.ExploreCategoryItemResponse
import com.digitaldukaan.services.serviceinterface.IExploreCategoryServiceInterface
import com.squareup.picasso.Picasso

class ExploreCategoryAdapter(
    private var mCategoryItemList: ArrayList<ExploreCategoryItemResponse>?,
    private var mCategoryItemClickListener: IExploreCategoryServiceInterface
) :
    RecyclerView.Adapter<ExploreCategoryAdapter.MarketingCardViewHolder>() {

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemContainer: View = itemView.findViewById(R.id.itemContainer)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        val view = MarketingCardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.explore_category_item, parent, false)
        )
        view.itemContainer.setOnClickListener {
            mCategoryItemClickListener.onExploreCategoryItemClick(
                mCategoryItemList?.get(view.adapterPosition)
            )
        }
        return view
    }

    override fun getItemCount(): Int = mCategoryItemList?.size ?: 0

    override fun onBindViewHolder(
        holder: MarketingCardViewHolder,
        position: Int
    ) {
        val item = mCategoryItemList?.get(position)
        holder.run {
            imageView?.let { Picasso.get().load(item?.imageUrl).into(it) }
        }
    }

}