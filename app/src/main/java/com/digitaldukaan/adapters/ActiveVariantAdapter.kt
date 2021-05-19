package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IVariantItemClickListener
import com.digitaldukaan.models.response.VariantItemResponse
import com.google.android.material.switchmaterial.SwitchMaterial

class ActiveVariantAdapter(
    private var mContext: Context?,
    private var mInStockText: String?,
    private var mActiveVariantList: ArrayList<VariantItemResponse>?,
    private var mListener: IVariantItemClickListener?
) :
    RecyclerView.Adapter<ActiveVariantAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val inStockTextView: TextView = itemView.findViewById(R.id.inStockTextView)
        val variantNameTextView: TextView = itemView.findViewById(R.id.variantNameTextView)
        val optionsMenuImageView: View = itemView.findViewById(R.id.optionsMenuImageView)
        val variantSwitch: SwitchMaterial = itemView.findViewById(R.id.variantSwitch)
        val variantView: ConstraintLayout = itemView.findViewById(R.id.constraintLayout8)
    }

    fun setActiveVariantList(activeVariantList: ArrayList<VariantItemResponse>?) {
        this.mActiveVariantList = activeVariantList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_active_variant_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mActiveVariantList?.size ?: 0

    override fun onBindViewHolder(
        holder: ReferAndEarnViewHolder,
        position: Int
    ) {
        val item = mActiveVariantList?.get(position)
        holder.apply {
            variantNameTextView.text = item?.variantName
            optionsMenuImageView.setOnClickListener {
                val menu = PopupMenu(mContext, it)
                menu.apply {
                    menuInflater.inflate(R.menu.menu_variant_options_menu, menu.menu)
                    show()
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem?.itemId) {
                            R.id.menu_edit -> mListener?.onVariantEditNameClicked(mActiveVariantList?.get(position), position)
                            R.id.menu_delete -> mListener?.onVariantDeleteClicked(position)
                        }
                        true
                    }
                }
            }
            variantSwitch.setOnCheckedChangeListener { _, isChecked ->
                item?.available = if (isChecked) 1 else 0
                if (isChecked) {
                    inStockTextView.text = mInStockText
                    variantView.isSelected = true
                    inStockTextView.alpha = 1f
                    variantNameTextView.alpha = 1f
                } else {
                    inStockTextView.text = mContext?.getString(R.string.out_of_stock)
                    variantView.isSelected = false
                    inStockTextView.alpha = 0.6f
                    variantNameTextView.alpha = 0.6f
                }
            }
            variantSwitch.isChecked = (item?.available == 1)
            if (variantSwitch.isChecked) {
                inStockTextView.text = mInStockText
                variantView.isSelected = true
                inStockTextView.alpha = 1f
                variantNameTextView.alpha = 1f
            } else {
                inStockTextView.text = mContext?.getString(R.string.out_of_stock)
                variantView.isSelected = false
                inStockTextView.alpha = 0.6f
                variantNameTextView.alpha = 0.6f
            }
        }
    }

    fun deleteItemFromActiveVariantList(position: Int) {
        mActiveVariantList?.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mActiveVariantList?.size ?: 0)
    }

}