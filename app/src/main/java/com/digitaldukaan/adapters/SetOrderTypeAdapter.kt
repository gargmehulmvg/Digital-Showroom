package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.models.response.SetOrderTypeItemResponse

class SetOrderTypeAdapter(
    private val mContext: Context?,
    private var setOrderTypeItemList: ArrayList<SetOrderTypeItemResponse>?
) :
    RecyclerView.Adapter<SetOrderTypeAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.set_order_type_item, parent, false)
        )
    }

    override fun getItemCount(): Int = setOrderTypeItemList?.size ?: 0

    override fun onBindViewHolder(
        holder: ReferAndEarnViewHolder,
        position: Int
    ) {
        holder.apply {
            mContext?.let {
                val item = setOrderTypeItemList?.get(position)
                when (item?.status) {
                    Constants.ORDER_STATUS_LOCKED -> {
                        textView.text = item.text
                        textView.setTextColor(ContextCompat.getColor(it, R.color.default_text_light_grey))
                        imageView.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_icon_material_lock))
                    }
                    Constants.ORDER_STATUS_IN_PROGRESS -> {
                        textView.text = item.text
                        textView.setTextColor(ContextCompat.getColor(it, R.color.black))
                        imageView.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_exclamation_small))
                    }
                    else -> {
                        textView.text = item?.text
                        textView.setTextColor(ContextCompat.getColor(it, R.color.open_green))
                        imageView.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_green_check_small))
                    }
                }
            }
        }
    }

}