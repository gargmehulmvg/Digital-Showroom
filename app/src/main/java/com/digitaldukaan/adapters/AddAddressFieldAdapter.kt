package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IAdapterItemNotifyListener
import com.digitaldukaan.models.response.AddressFieldsItemResponse
import com.google.android.material.switchmaterial.SwitchMaterial

class AddAddressFieldAdapter(
    private var mItemList: ArrayList<AddressFieldsItemResponse?>?,
    private var mListener: IAdapterItemNotifyListener?
    ) : RecyclerView.Adapter<AddAddressFieldAdapter.AddAddressFieldViewHolder>() {

    inner class AddAddressFieldViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mandatorySwitch: SwitchMaterial = itemView.findViewById(R.id.mandatorySwitch)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val mandatoryTextView: TextView = itemView.findViewById(R.id.mandatoryTextView)
        val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)
        val subHeadingTextView: TextView = itemView.findViewById(R.id.subHeadingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddAddressFieldViewHolder =
        AddAddressFieldViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.add_address_field_item, parent, false))

    override fun getItemCount(): Int = mItemList?.size ?: 0

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    override fun onBindViewHolder(holder: AddAddressFieldViewHolder, position: Int) {
        val item = mItemList?.get(position)
        holder.apply {
            headingTextView.text = item?.heading
            subHeadingTextView.apply {
                visibility = if (isNotEmpty(item?.subHeading)) View.VISIBLE else View.GONE
                text = item?.subHeading
            }
            mandatoryTextView.text = item?.textMandatory
            mandatoryTextView.alpha = if (true == item?.isFieldSelected) 1f else 0.28f
            checkBox.apply {
                isChecked = item?.isFieldSelected ?: false
                alpha = if (true == item?.isFieldEnabled) 1f else 0.28f
                setOnClickListener {
                    mListener?.onAdapterItemNotifyListener(position)
                    if (false == item?.isFieldEnabled) {
                        checkBox.isChecked = !checkBox.isChecked
                        return@setOnClickListener
                    }
                    item?.isFieldSelected = checkBox.isChecked
                    mandatoryTextView.alpha = if (checkBox.isChecked) 1f else 0.28f
                    if (!checkBox.isChecked) {
                        mandatorySwitch.isChecked = false
                    }
                }
            }
            mandatorySwitch.apply {
                mListener?.onAdapterItemNotifyListener(position)
                isChecked = item?.isMandatory ?: false
                mandatorySwitch.alpha = if (true == item?.isFieldEnabled) 1f else 0.28f
                checkBox.alpha = if (false == item?.isFieldEnabled) 0.28f else 1f

                setOnClickListener {
                    mListener?.onAdapterItemNotifyListener(position)
                    if (false == item?.isFieldEnabled) {
                        mandatorySwitch.isChecked = !mandatorySwitch.isChecked
                        return@setOnClickListener
                    }
                    item?.isMandatory = mandatorySwitch.isChecked
                    if (mandatorySwitch.isChecked) {
                        checkBox.isChecked = true
                    }
                    mandatoryTextView.alpha = if (checkBox.isChecked) 1f else 0.28f
                }
            }
            headingTextView.setOnClickListener {
                mListener?.onAdapterItemNotifyListener(position)
                if (true == item?.isFieldEnabled) updateCheckBoxAndSwitch(item)
            }
            subHeadingTextView.setOnClickListener {
                mListener?.onAdapterItemNotifyListener(position)
                if (true == item?.isFieldEnabled) updateCheckBoxAndSwitch(item)
            }
        }
    }

    private fun AddAddressFieldViewHolder.updateCheckBoxAndSwitch(item: AddressFieldsItemResponse?) {
        item?.isFieldSelected = !(checkBox.isChecked)
        checkBox.isChecked = !(checkBox.isChecked)
        mandatorySwitch.isChecked = checkBox.isChecked
        item?.isMandatory = checkBox.isChecked
    }

}