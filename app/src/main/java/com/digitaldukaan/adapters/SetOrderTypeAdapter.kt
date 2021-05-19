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

class SetOrderTypeAdapter(
    private val mContext: Context?
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

    override fun getItemCount(): Int = 3

    override fun onBindViewHolder(
        holder: ReferAndEarnViewHolder,
        position: Int
    ) {
        holder.apply {
            mContext?.let {
                if (position == 0) {
                    textView.setTextColor(ContextCompat.getColor(it, R.color.default_text_light_grey))
                    imageView.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_icon_material_lock))
                } else {
                    textView.text = "Set Delivery charge to fixed (for delivery orders)"
                    textView.setTextColor(ContextCompat.getColor(it, R.color.black))
                    imageView.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_exclamation_small))
                }
            }
        }
    }

}