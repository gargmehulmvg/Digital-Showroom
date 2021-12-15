package com.digitaldukaan.adapters

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IProductInventoryListener
import com.digitaldukaan.models.dto.InventoryEnableDTO
import com.google.android.material.textfield.TextInputLayout

class InventoryEnableAdapter(
    private var mQuantityErrorMessage: String?,
    private var mItemList: ArrayList<InventoryEnableDTO>,
    private var mListener: IProductInventoryListener
) : RecyclerView.Adapter<InventoryEnableAdapter.InventoryEnableViewHolder>() {

    inner class InventoryEnableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textInputLayout: TextInputLayout = itemView.findViewById(R.id.textInputLayout)
        val editText: EditText = itemView.findViewById(R.id.editText)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }

    companion object {
        private const val TAG = "InventoryEnableAdapter"
    }

    fun getDataSource(): ArrayList<InventoryEnableDTO> {
        return mItemList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryEnableViewHolder {
        return InventoryEnableViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.inventory_enable_item, parent, false))
    }

    override fun getItemCount(): Int = mItemList.size

    override fun onBindViewHolder(holder: InventoryEnableAdapter.InventoryEnableViewHolder, position: Int) {
        val pos = position ?: 0
        val item = mItemList[pos]
        holder.textInputLayout.hint = item.inventoryName
        holder.editText.apply {
            val countValue = if (0 == item.inventoryCount) null else "${item.inventoryCount}"
            setText(countValue)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    Log.d(TAG, "beforeTextChanged: ")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    Log.d(TAG, "onTextChanged: ")
                }

                override fun afterTextChanged(editable: Editable?) {
                    if (isEmpty(mItemList) || pos < 0) return
                    val str = editable?.toString()?.trim() ?: ""
                    if (str.length <= 3) {
                        if (!mItemList[pos].isVariantEnabled) {
                            mListener.onItemInventoryChangeListener(if (isNotEmpty(str)) {
                                mItemList[pos].inventoryCount = str.toInt()
                                str
                            } else {
                                mItemList[pos].inventoryCount = 0
                                "0"
                            })
                        }
                    } else {
                        error = mQuantityErrorMessage
                        text = null
                    }
                }

            })
        }
        holder.checkBox.apply {
            if (item.isVariantEnabled) {
                visibility = View.VISIBLE
                isEnabled = (item.isEnabled)
                alpha = if (item.isEnabled) 1f else 0.25f
            } else
                visibility = View.GONE
        }
        holder.textInputLayout.isEnabled = (item.isEnabled)
    }

}