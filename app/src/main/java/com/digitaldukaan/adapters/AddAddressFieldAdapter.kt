package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.models.response.AddressFieldsItemResponse
import com.google.android.material.switchmaterial.SwitchMaterial

class AddAddressFieldAdapter(private var mItemList: ArrayList<AddressFieldsItemResponse?>?) : RecyclerView.Adapter<AddAddressFieldAdapter.AddAddressFieldViewHolder>() {

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
            checkBox.apply {
                isChecked = item?.isFieldSelected ?: false
                isEnabled = item?.isFieldEnabled ?: false
                setOnCheckedChangeListener { _, isChecked -> item?.isFieldSelected = isChecked }
            }
            mandatorySwitch.apply {
                isChecked = item?.isMandatory ?: false
                isEnabled = item?.isMandatoryEnabled ?: false
                setOnCheckedChangeListener { _, isChecked -> item?.isMandatory = isChecked }
            }
        }
    }

}