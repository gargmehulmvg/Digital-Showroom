package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.models.dto.CustomerDeliveryAddressDTO

class CustomerDeliveryAddressAdapter(
    private var mCustomerDeliveryAddressList: ArrayList<CustomerDeliveryAddressDTO>?
) :
    RecyclerView.Adapter<CustomerDeliveryAddressAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerDetailItemLabel: TextView = itemView.findViewById(R.id.customerDetailItemLabel)
        val customerDetailItemValue: TextView = itemView.findViewById(R.id.customerDetailItemValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.customer_detail_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mCustomerDeliveryAddressList?.size ?: 0

    override fun onBindViewHolder(
        holder: ReferAndEarnViewHolder,
        position: Int
    ) {
        val item = mCustomerDeliveryAddressList?.get(position)
        holder.apply {
            customerDetailItemLabel.text = item?.customerDeliveryAddressLabel
            customerDetailItemValue.text = if (item?.customerDeliveryAddressValue == null) "" else item.customerDeliveryAddressValue
        }
    }

}