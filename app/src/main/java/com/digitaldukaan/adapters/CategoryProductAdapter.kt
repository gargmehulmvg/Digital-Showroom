package com.digitaldukaan.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.models.response.AddStoreCategoryItem
import com.digitaldukaan.models.response.MarketingStaticTextResponse

class CategoryProductAdapter(
    private var mMarketingStaticTextResponse: MarketingStaticTextResponse?,
    private var mCategoryNameList: ArrayList<AddStoreCategoryItem>?
) :
    RecyclerView.Adapter<CategoryProductAdapter.CustomDomainSelectionViewHolder>() {

    inner class CustomDomainSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomDomainSelectionViewHolder {
        return CustomDomainSelectionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.category_product_item, parent, false))
    }

    override fun getItemCount(): Int = mCategoryNameList?.size ?: 0

    override fun onBindViewHolder(holder: CustomDomainSelectionViewHolder, position: Int) {
        val item = mCategoryNameList?.get(position)
        holder.apply {
            Log.d("CustomDomainSelectionAdapter", "onBindViewHolder: $position :: $item")
            categoryNameTextView.text = if (0 == position) mMarketingStaticTextResponse?.text_recently_added else mCategoryNameList?.get(position)?.name

        }
    }

}