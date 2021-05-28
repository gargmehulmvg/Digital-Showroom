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
import com.digitaldukaan.interfaces.IRecyclerViewClickListener
import com.digitaldukaan.models.response.SetOrderTypeItemResponse

class SetOrderTypeAdapter(
    private val mContext: Context?,
    private var setOrderTypeItemList: ArrayList<SetOrderTypeItemResponse>?,
    private var mode: String,
    private var listener: IRecyclerViewClickListener?
) :
    RecyclerView.Adapter<SetOrderTypeAdapter.SetOrderTypeViewHolder>() {

    inner class SetOrderTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val container: View = itemView.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetOrderTypeViewHolder {
        val view = SetOrderTypeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.set_order_type_item, parent, false))
        view.container.setOnClickListener {
            listener?.onRecyclerViewClickListener(mode)
        }
        return view
    }

    override fun getItemCount(): Int = setOrderTypeItemList?.size ?: 0

    override fun onBindViewHolder(
        holder: SetOrderTypeViewHolder,
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