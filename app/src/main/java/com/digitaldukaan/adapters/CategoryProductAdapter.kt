package com.digitaldukaan.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.IProductItemClickListener
import com.digitaldukaan.models.response.MarketingStaticTextResponse
import com.digitaldukaan.models.response.ProductCategoryCombineResponse

class CategoryProductAdapter(
    private val mContext: Context?,
    private val mMarketingStaticTextResponse: MarketingStaticTextResponse?,
    private var mCompleteList: ArrayList<ProductCategoryCombineResponse>?,
    private val mListener: IProductItemClickListener
) :
    RecyclerView.Adapter<CategoryProductAdapter.CategoryProductViewHolder>() {

    fun setProductCategoryList(list: ArrayList<ProductCategoryCombineResponse>?) {
        this.mCompleteList = list
        notifyDataSetChanged()
    }

    inner class CategoryProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductViewHolder {
        return CategoryProductViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.category_product_item, parent, false))
    }

    override fun getItemCount(): Int = mCompleteList?.size ?: 0

    override fun onBindViewHolder(holder: CategoryProductViewHolder, position: Int) {
        val item = mCompleteList?.get(position)
        holder.apply {
            Log.d("CustomDomainSelectionAdapter", "onBindViewHolder: $position :: $item")
            categoryNameTextView.text = if (isEmpty(mCompleteList?.get(position)?.category?.name)) mMarketingStaticTextResponse?.text_recently_added else mCompleteList?.get(position)?.category?.name
            recyclerView.apply {
                isNestedScrollingEnabled = false
                layoutManager = LinearLayoutManager(mContext)
                adapter = ProductsAdapter(mContext, mCompleteList?.get(position)?.productsList, mListener)
            }
        }
    }

}