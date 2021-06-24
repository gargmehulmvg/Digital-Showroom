package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.OrderNotificationItemResponse

class OrderNotificationsAdapter(
    private val mContext: Context?,
    private var itemList: ArrayList<OrderNotificationItemResponse>?,
    private var listener: IAdapterItemClickListener?
) :
    RecyclerView.Adapter<OrderNotificationsAdapter.OrderNotificationsViewHolder>() {

    inner class OrderNotificationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioButton: RadioButton = itemView.findViewById(R.id.completeImageView)
        val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)
        val subHeadingTextView: TextView = itemView.findViewById(R.id.subHeadingTextView)
        val container: View = itemView.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderNotificationsViewHolder {
        val view = OrderNotificationsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_order_notification_item, parent, false))
        view.container.setOnClickListener { listener?.onAdapterItemClickListener(view.adapterPosition) }
        return view
    }

    override fun getItemCount(): Int = itemList?.size ?: 0

    override fun onBindViewHolder(
        holder: OrderNotificationsViewHolder,
        position: Int
    ) {
        holder.apply {
            mContext?.let { context ->
                val item = itemList?.get(position)
                headingTextView.text = item?.heading
                if (isEmpty(item?.subHeading)) {
                    subHeadingTextView.visibility = View.GONE
                } else {
                    subHeadingTextView.visibility = View.VISIBLE
                    subHeadingTextView.text = item?.subHeading
                }
                if (item?.isSelected == true) {
                    container.elevation = 1f
                    radioButton.isChecked = true
                    container.background = ContextCompat.getDrawable(context, R.drawable.ripple_slight_curve_grey_white_background_green_border)
                    headingTextView.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                } else {
                    container.elevation = 10f
                    radioButton.isChecked = false
                    headingTextView.setTextColor(ContextCompat.getColor(context, R.color.black))
                    container.background = ContextCompat.getDrawable(context, R.drawable.ripple_slight_curve_grey_white_background)
                }
            }
        }
    }

}