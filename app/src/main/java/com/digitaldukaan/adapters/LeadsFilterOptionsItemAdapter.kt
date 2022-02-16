package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.ILeadsFilterItemClickListener
import com.digitaldukaan.models.response.LeadsFilterOptionsItemResponse

class LeadsFilterOptionsItemAdapter(
    private var mContext: Context?,
    private var mFilterItemList: ArrayList<LeadsFilterOptionsItemResponse>?,
    private var mListener: ILeadsFilterItemClickListener,
    private var mFilterType: String?
) : RecyclerView.Adapter<LeadsFilterOptionsItemAdapter.LeadsFilterViewHolder>() {

    inner class LeadsFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.itemTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeadsFilterViewHolder {
        val view = LeadsFilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bottom_sheet_filter_options_item, parent, false))
        view.textView.setOnClickListener {
            if (view.adapterPosition < 0 || view.adapterPosition >= (mFilterItemList?.size ?: 0)) return@setOnClickListener
            mFilterItemList?.forEachIndexed { position, itemResponse ->
                itemResponse.isSelected = (view.adapterPosition == position)
            }
            mListener.onLeadsFilterItemClickListener(mFilterItemList?.get(view.adapterPosition), mFilterType)
        }
        return view
    }

    override fun getItemCount(): Int = mFilterItemList?.size ?: 0

    override fun onBindViewHolder(holder: LeadsFilterViewHolder, position: Int) {
        holder.apply {
            val item = mFilterItemList?.get(position)
            textView.text = if (isNotEmpty(item?.customDateRangeStr)) {
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_pencil, 0)
                item?.customDateRangeStr
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                item?.text
            }
            mContext?.let { context ->
                textView.background = ContextCompat.getDrawable(context, if (true == item?.isSelected) R.drawable.selected_chip_blue_border_bluish_background else R.drawable.ripple_full_curve_white_background_grey_border)
            }
        }
    }
}