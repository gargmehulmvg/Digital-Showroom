package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.models.response.ExploreCategoryStaticTextResponse
import com.digitaldukaan.models.response.MasterCatalogItemResponse
import com.digitaldukaan.services.serviceinterface.IExploreCategoryServiceInterface
import com.squareup.picasso.Picasso

class MasterCatalogItemsAdapter(
    private val mActivity: MainActivity,
    private var mCategoryItemList: ArrayList<MasterCatalogItemResponse>?,
    private var mCategoryItemClickListener: IExploreCategoryServiceInterface,
    private var mStaticText: ExploreCategoryStaticTextResponse? = null
) :
    RecyclerView.Adapter<MasterCatalogItemsAdapter.MarketingCardViewHolder>() {

    private var mSelectedProductsHashMap: HashMap<Int?, MasterCatalogItemResponse?>? = HashMap()
    private var mSubCategoryLimitMap: HashMap<Int, Int> = HashMap()
    private var mSubCategoryLimit = 0
    private var mCategoryId = 0

    fun setMasterCatalogList(list: ArrayList<MasterCatalogItemResponse>?, selectedProductsHashMap: HashMap<Int?, MasterCatalogItemResponse?>?, subCategoryLimitMap: HashMap<Int, Int>, subCategoryLimit: Int, categoryId: Int) {
        this.mCategoryItemList = list
        this.mSelectedProductsHashMap = selectedProductsHashMap
        this.mSubCategoryLimitMap = subCategoryLimitMap
        this.mSubCategoryLimit = subCategoryLimit
        this.mCategoryId = categoryId
        notifyDataSetChanged()
    }

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val setPriceTextView: TextView = itemView.findViewById(R.id.setPriceTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val container: View = itemView.findViewById(R.id.container)
        val checkBox: CheckBox = itemView.findViewById(R.id.businessTypeCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        val view = MarketingCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.master_catalog_item, parent, false))
        view.imageView.setOnClickListener { mCategoryItemClickListener.onCategoryItemsImageClick(mCategoryItemList?.get(view.absoluteAdapterPosition)) }
        view.setPriceTextView.setOnClickListener {
            val item = mCategoryItemList?.get(view.absoluteAdapterPosition)
            if (isSubCategoryItemSelectionExceeded(view.checkBox.isChecked)) return@setOnClickListener
            if (mActivity.resources.getInteger(R.integer.max_selection_item_catalog) == mSelectedProductsHashMap?.size && !view.checkBox.isChecked) {
                mActivity.showToast(mStaticText?.text_error_cart_limit)
                return@setOnClickListener
            }
            if (true == item?.isAdded) return@setOnClickListener
            mCategoryItemClickListener.onCategoryItemsSetPriceClick(view.absoluteAdapterPosition, mCategoryItemList?.get(view.absoluteAdapterPosition))
        }
        view.priceTextView.setOnClickListener {
            val item = mCategoryItemList?.get(view.absoluteAdapterPosition)
            if (isSubCategoryItemSelectionExceeded(view.checkBox.isChecked)) return@setOnClickListener
            if (mActivity.resources.getInteger(R.integer.max_selection_item_catalog) == mSelectedProductsHashMap?.size && !view.checkBox.isChecked) {
                mActivity.showToast(mStaticText?.text_error_cart_limit)
                return@setOnClickListener
            }
            if (0.0 != item?.price) return@setOnClickListener
            if (item.isAdded) return@setOnClickListener
            mCategoryItemClickListener.onCategoryItemsSetPriceClick(view.absoluteAdapterPosition, item)
        }
        view.checkBox.setOnClickListener {
            val item = mCategoryItemList?.get(view.absoluteAdapterPosition)
            if (isSubCategoryItemSelectionExceeded(!view.checkBox.isChecked)) {
                view.checkBox.isChecked = false
                return@setOnClickListener
            }
            if (mActivity.resources.getInteger(R.integer.max_selection_item_catalog) == mSelectedProductsHashMap?.size && view.checkBox.isChecked) {
                mActivity.showToast(mStaticText?.text_error_cart_limit)
                view.checkBox.isChecked = false
                view.checkBox.isSelected = false
            }  else {
                if (true == item?.isAdded) {
                    view.checkBox.isChecked = false
                    view.checkBox.isSelected = false
                    return@setOnClickListener
                }
                mCategoryItemClickListener.onCategoryCheckBoxClick(view.absoluteAdapterPosition, item, view.checkBox.isChecked)
            }
        }
        view.titleTextView.setOnClickListener {
            val item = mCategoryItemList?.get(view.absoluteAdapterPosition)
            if (isSubCategoryItemSelectionExceeded(view.checkBox.isChecked)) return@setOnClickListener
            if (mActivity.resources.getInteger(R.integer.max_selection_item_catalog) == mSelectedProductsHashMap?.size && !view.checkBox.isChecked) {
                mActivity.showToast(mStaticText?.text_error_cart_limit)
                view.checkBox.isChecked = false
                view.checkBox.isSelected = false
            } else {
                mCategoryItemClickListener.onCategoryCheckBoxClick(view.absoluteAdapterPosition, item, !view.checkBox.isChecked)
                view.checkBox.isChecked = !view.checkBox.isChecked
            }
        }
        return view
    }

    private fun isSubCategoryItemSelectionExceeded(isChecked: Boolean): Boolean {
        if (null != mSubCategoryLimitMap[mCategoryId] && (mSubCategoryLimit == (mSubCategoryLimitMap[mCategoryId])) && !isChecked) {
            mActivity.showToast(mStaticText?.text_error_sub_category_limit)
            return true
        }
        return false
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    override fun getItemCount(): Int = mCategoryItemList?.size ?: 0

    override fun onBindViewHolder(holder: MarketingCardViewHolder, position: Int) {
        var item = mCategoryItemList?.get(position)
        if (true == mSelectedProductsHashMap?.containsKey(item?.itemId)) {
            item = mSelectedProductsHashMap?.get(item?.itemId)
        }
        holder.run {
            Picasso.get().load(item?.imageUrl).into(imageView)
            titleTextView.text = item?.itemName
            val priceStr = "${mStaticText?.text_rupees_symbol} ${item?.price}"
            priceTextView.text = priceStr
            titleTextView.setTextColor(ContextCompat.getColor(mActivity, if (item?.isSelected == true) R.color.open_green else R.color.black))
            container.isEnabled = (false == item?.isAdded)
            container.alpha = if (true == item?.isAdded) 0.2f else 1f
            checkBox.isEnabled = (false == item?.isAdded)
            if (0.0 == item?.price) {
                val priceString = "${mStaticText?.text_set_your_price} ${mStaticText?.text_rupees_symbol}"
                priceTextView.text = priceString
                val emptySpace = "_____"
                setPriceTextView.text = emptySpace
            } else {
                setPriceTextView.text = null
                checkBox.isEnabled = true
            }
            checkBox.isChecked = (true == item?.isSelected || true == mSelectedProductsHashMap?.containsKey(item?.itemId))
        }
    }
}