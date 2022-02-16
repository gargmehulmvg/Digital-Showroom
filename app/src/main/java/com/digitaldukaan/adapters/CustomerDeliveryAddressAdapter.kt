package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.dto.CustomerDeliveryAddressDTO

class CustomerDeliveryAddressAdapter(
    private var mContext: Context?,
    private var mCustomerDeliveryAddressList: ArrayList<CustomerDeliveryAddressDTO>?,
    private var mListener: IAdapterItemClickListener
) :
    RecyclerView.Adapter<CustomerDeliveryAddressAdapter.CustomerDeliveryAddressViewHolder>() {

    inner class CustomerDeliveryAddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerDetailItemLabel: TextView = itemView.findViewById(R.id.customerDetailItemLabel)
        val customerDetailItemValue: TextView = itemView.findViewById(R.id.customerDetailItemValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerDeliveryAddressViewHolder {
        val view = CustomerDeliveryAddressViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.customer_detail_item, parent, false)
        )
        view.customerDetailItemValue.setOnClickListener {
            mListener.onAdapterItemClickListener(view.adapterPosition)
        }
        return view
    }

    override fun getItemCount(): Int = mCustomerDeliveryAddressList?.size ?: 0

    override fun onBindViewHolder(
        holder: CustomerDeliveryAddressViewHolder,
        position: Int
    ) {
        val item = mCustomerDeliveryAddressList?.get(position)
        holder.apply {
            customerDetailItemLabel.text = item?.customerDeliveryAddressLabel
            customerDetailItemValue.text = if (isEmpty(item?.customerDeliveryAddressValue)) "" else item?.customerDeliveryAddressValue
            mContext?.let { context -> customerDetailItemValue.setTextColor(ContextCompat.getColor(context, if (Constants.ACTION_EMAIL == item?.customerDeliveryAddressAction) R.color.primary_blue else R.color.black)) }
        }
    }

}