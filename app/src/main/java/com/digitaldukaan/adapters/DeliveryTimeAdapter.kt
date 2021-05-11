package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.models.response.DeliveryTimeItemResponse

class DeliveryTimeAdapter(
    private var mDeliveryTimeList: ArrayList<DeliveryTimeItemResponse>?,
    private var mListener: IChipItemClickListener
) :
    RecyclerView.Adapter<DeliveryTimeAdapter.AddProductsChipsViewHolder>() {

    fun setDeliveryTimeList(list: ArrayList<DeliveryTimeItemResponse>?) {
        mDeliveryTimeList = list
        notifyDataSetChanged()
    }

    inner class AddProductsChipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chipTextView: TextView = itemView.findViewById(R.id.chipTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddProductsChipsViewHolder {
        val holder = AddProductsChipsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.delivery_time_layout, parent, false)
        )
        holder.chipTextView.setOnClickListener { mListener.onChipItemClickListener(holder.adapterPosition) }
        return holder
    }

    override fun getItemCount(): Int = mDeliveryTimeList?.size ?: 0

    override fun onBindViewHolder(
        holder: AddProductsChipsViewHolder,
        position: Int
    ) {
        holder.apply {
            val item = mDeliveryTimeList?.get(position)
            chipTextView.text = item?.value
            chipTextView.isSelected = item?.isSelected == true
        }
    }

}