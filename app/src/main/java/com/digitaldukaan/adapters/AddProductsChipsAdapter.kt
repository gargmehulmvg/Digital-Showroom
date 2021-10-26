package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.models.response.StoreCategoryItem

class AddProductsChipsAdapter(
    private var mAddProductStoreCategoryList: ArrayList<StoreCategoryItem>?,
    private var mListener: IChipItemClickListener
) :
    RecyclerView.Adapter<AddProductsChipsAdapter.AddProductsChipsViewHolder>() {

    fun setAddProductStoreCategoryList(list: ArrayList<StoreCategoryItem>?) {
        mAddProductStoreCategoryList = list
        notifyDataSetChanged()
    }

    inner class AddProductsChipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chipTextView: TextView = itemView.findViewById(R.id.chipTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddProductsChipsViewHolder {
        val holder = AddProductsChipsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.chip_item_layout2, parent, false)
        )
        holder.chipTextView.setOnClickListener {
            val position = holder.adapterPosition
            if (position < 0) return@setOnClickListener
            if (isEmpty(mAddProductStoreCategoryList)) return@setOnClickListener
            if (position > mAddProductStoreCategoryList?.size ?: 0) return@setOnClickListener
            mListener.onChipItemClickListener(position)
        }
        return holder
    }

    override fun getItemCount(): Int = mAddProductStoreCategoryList?.size ?: 0

    override fun onBindViewHolder(
        holder: AddProductsChipsViewHolder,
        position: Int
    ) {
        holder.apply {
            chipTextView.text = mAddProductStoreCategoryList?.get(position)?.name
            if (mAddProductStoreCategoryList?.get(position)?.isSelected == true) {
                chipTextView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_check_small,
                    0,
                    0,
                    0
                )
                chipTextView.isSelected = true
            } else {
                chipTextView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    0,
                    0
                )
                chipTextView.isSelected = false
            }
        }
    }

}