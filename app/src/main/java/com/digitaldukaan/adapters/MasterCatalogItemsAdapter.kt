package com.digitaldukaan.adapters

import android.os.Build
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
import com.digitaldukaan.models.response.AddProductStaticText
import com.digitaldukaan.models.response.MasterCatalogItemResponse
import com.digitaldukaan.services.serviceinterface.IExploreCategoryServiceInterface
import com.squareup.picasso.Picasso

class MasterCatalogItemsAdapter(
    private val mActivity: MainActivity,
    private var mCategoryItemList: ArrayList<MasterCatalogItemResponse>?,
    private var mCategoryItemClickListener: IExploreCategoryServiceInterface,
    private var mStaticText: AddProductStaticText? = null
) :
    RecyclerView.Adapter<MasterCatalogItemsAdapter.MarketingCardViewHolder>() {

    fun setSubCategoryList(list: ArrayList<MasterCatalogItemResponse>?) {
        this.mCategoryItemList = list
        notifyDataSetChanged()
    }

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val container: View = itemView.findViewById(R.id.container)
        val checkBox: CheckBox = itemView.findViewById(R.id.businessTypeCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        val view = MarketingCardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.master_catelog_item, parent, false)
        )
        view.imageView.setOnClickListener {
            mCategoryItemClickListener.onCategoryItemsClickResponse(mCategoryItemList?.get(view.adapterPosition))
        }
        view.priceTextView.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.tooltipText =  "You can change the price after adding to your catalogue"
            }
        }
        return view
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int = mCategoryItemList?.size ?: 0

    override fun onBindViewHolder(
        holder: MarketingCardViewHolder,
        position: Int
    ) {
        val item = mCategoryItemList?.get(position)
        holder.run {
            Picasso.get().load(item?.imageUrl).into(imageView)
            titleTextView.text = item?.itemName
            priceTextView.text = "${mStaticText?.text_rupees_symbol} ${item?.price}"
            titleTextView.setTextColor(ContextCompat.getColor(mActivity, if (item?.isSelected == true) R.color.open_green else R.color.black))
            if (item?.isAdded == true) {
                container.isEnabled = false
                container.alpha = 0.2f
                checkBox.isEnabled = false
            }
        }
    }

}