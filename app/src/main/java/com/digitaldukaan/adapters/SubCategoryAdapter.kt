package com.digitaldukaan.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.models.response.ExploreCategoryItemResponse
import com.digitaldukaan.services.serviceinterface.IExploreCategoryServiceInterface
import com.squareup.picasso.Picasso

class SubCategoryAdapter(
    private val mActivity: MainActivity,
    private var mCategoryItemList: ArrayList<ExploreCategoryItemResponse>?,
    private var mCategoryItemClickListener: IExploreCategoryServiceInterface
) :
    RecyclerView.Adapter<SubCategoryAdapter.MarketingCardViewHolder>() {

    fun setSubCategoryList(list: ArrayList<ExploreCategoryItemResponse>?) {
        this.mCategoryItemList = list
        notifyDataSetChanged()
    }

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectedView: View = itemView.findViewById(R.id.selectedView)
        val itemContainer: View = itemView.findViewById(R.id.itemContainer)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        val view = MarketingCardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.sub_category_item, parent, false)
        )
        view.itemContainer.setOnClickListener {
            mCategoryItemClickListener.onExploreCategoryItemClick(mCategoryItemList?.get(view.adapterPosition))
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
            imageView?.let {
                try {
                    Picasso.get().load(item?.imageUrl).into(it)
                } catch (e: Exception) {
                    Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                }
            }
            titleTextView.text = item?.categoryName
            titleTextView.setTextColor(ContextCompat.getColor(mActivity, if (item?.isSelected == true) R.color.open_green else R.color.black))
            selectedView.visibility = if (item?.isSelected == true) View.VISIBLE else View.INVISIBLE
        }
    }

}