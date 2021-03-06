package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IAddressFieldsItemListener
import com.digitaldukaan.models.response.AddressFieldsItemResponse
import com.google.android.material.switchmaterial.SwitchMaterial

class AddAddressFieldAdapter(
    private var mItemList: ArrayList<AddressFieldsItemResponse?>?,
    private var mListener: IAddressFieldsItemListener?
    ) : RecyclerView.Adapter<AddAddressFieldAdapter.AddAddressFieldViewHolder>() {

    companion object {
        private const val VISIBILITY_FULL = 1f
        private const val VISIBILITY_BLUR = 0.28f
    }

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
            mandatoryTextView.alpha = if (true == item?.isFieldSelected) VISIBILITY_FULL else VISIBILITY_BLUR
            checkBox.apply {
                isChecked = item?.isFieldSelected ?: false
                alpha = if (true == item?.isFieldEnabled) VISIBILITY_FULL else VISIBILITY_BLUR
                setOnClickListener {
                    mListener?.onAddressFieldsItemNotifyListener(position)
                    if (false == item?.isFieldEnabled) {
                        checkBox.isChecked = !checkBox.isChecked
                        return@setOnClickListener
                    }
                    item?.isFieldSelected = checkBox.isChecked
                    mandatoryTextView.alpha = if (checkBox.isChecked) VISIBILITY_FULL else VISIBILITY_BLUR
                    if (!checkBox.isChecked) {
                        mandatorySwitch.isChecked = false
                    }
                    mListener?.onAddressFieldsCheckChangeListener(item)
                }
            }
            mandatorySwitch.apply {
                isChecked = item?.isMandatory ?: false
                mandatorySwitch.alpha = if (true == item?.isFieldEnabled) VISIBILITY_FULL else VISIBILITY_BLUR
                checkBox.alpha = if (false == item?.isFieldEnabled) VISIBILITY_BLUR else VISIBILITY_FULL
                setOnClickListener {
                    mListener?.onAddressFieldsItemNotifyListener(position)
                    if (false == item?.isFieldEnabled) {
                        mandatorySwitch.isChecked = !mandatorySwitch.isChecked
                        return@setOnClickListener
                    }
                    item?.isMandatory = mandatorySwitch.isChecked
                    if (mandatorySwitch.isChecked) {
                        checkBox.isChecked = true
                    }
                    mandatoryTextView.alpha = if (checkBox.isChecked) VISIBILITY_FULL else VISIBILITY_BLUR
                    mListener?.onAddressFieldsMandatorySwitchChangeListener(item)
                }
            }
            headingTextView.setOnClickListener {
                mListener?.onAddressFieldsItemNotifyListener(position)
                if (true == item?.isFieldEnabled) updateCheckBoxAndSwitch(item)
            }
            subHeadingTextView.setOnClickListener {
                mListener?.onAddressFieldsItemNotifyListener(position)
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