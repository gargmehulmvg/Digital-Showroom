package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.models.response.DeleteCategoryItemResponse

class DeleteCategoryAdapter(
    private var mDeliveryTimeList: ArrayList<DeleteCategoryItemResponse?>?,
    private var mListener: IChipItemClickListener
) :
    RecyclerView.Adapter<DeleteCategoryAdapter.AddProductsChipsViewHolder>() {

    inner class AddProductsChipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deleteCategoryTextView: TextView = itemView.findViewById(R.id.deleteCategoryTextView)
        val container: View = itemView.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddProductsChipsViewHolder {
        val holder = AddProductsChipsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.delete_category_layout, parent, false)
        )
        holder.container.setOnClickListener { mListener.onChipItemClickListener(holder.adapterPosition) }
        return holder
    }

    override fun getItemCount(): Int = mDeliveryTimeList?.size ?: 0

    override fun onBindViewHolder(
        holder: AddProductsChipsViewHolder,
        position: Int
    ) {
        holder.apply {
            val item = mDeliveryTimeList?.get(position)
            deleteCategoryTextView.text = item?.text
        }
    }

}