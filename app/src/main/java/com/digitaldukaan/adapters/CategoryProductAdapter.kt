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
import com.digitaldukaan.interfaces.IProductItemClickListener
import com.digitaldukaan.models.response.MarketingStaticTextResponse
import com.digitaldukaan.models.response.ProductCategoryCombineResponse

class CategoryProductAdapter(
    private val mContext: Context?,
    private val mMarketingStaticTextResponse: MarketingStaticTextResponse?,
    private val mCompleteList: ArrayList<ProductCategoryCombineResponse>?,
    private val mListener: IProductItemClickListener
) :
    RecyclerView.Adapter<CategoryProductAdapter.CategoryProductViewHolder>() {

    companion object {
        private const val TAG = "CategoryProductAdapter"
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
            categoryNameTextView.text = if (0 == position) mMarketingStaticTextResponse?.text_recently_added else mCompleteList?.get(position)?.category?.name
            recyclerView.apply {
                isNestedScrollingEnabled = false
                layoutManager = LinearLayoutManager(mContext)
                adapter = ProductsAdapter(mContext, mCompleteList?.get(position)?.productsList, mListener)
            }
//            initiateProductsApiCall(item?.id ?: 0, holder)
        }
    }

    /*private fun initiateProductsApiCall(id: Int, holder: CategoryProductViewHolder) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val response = RetrofitApi().getServerCallObject()?.getProductsByCategoryId(id)
                response?.let {
                    if (it.isSuccessful) {
                        it.body()?.let {
                            CoroutineScopeUtils().runTaskOnCoroutineMain {
                                if (it.mIsSuccessStatus) {
                                    val listType = object : TypeToken<ArrayList<ProductResponse>>() {}.type
                                    val productsList = Gson().fromJson<ArrayList<ProductResponse>>(it.mCommonDataStr, listType)
                                    Log.d(TAG, "initiateProductsApiCall: request :: ID :: $id response :: $productsList")
                                    holder.recyclerView.apply {
                                        layoutManager = LinearLayoutManager(mContext)
                                        adapter = ProductsAdapter(mContext, productsList, mListener)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "initiateProductsApiCall: ${e.message}", e)
            }
        }
    }*/

}