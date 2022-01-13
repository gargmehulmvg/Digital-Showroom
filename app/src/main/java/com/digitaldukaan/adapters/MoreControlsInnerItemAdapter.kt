package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.constants.setHtmlData
import com.digitaldukaan.models.response.MoreControlsInnerItemResponse
import com.digitaldukaan.models.response.MoreControlsStaticTextResponse

class MoreControlsInnerItemAdapter(
    private var mContext: Context?,
    private var mStoreControlItemsList: ArrayList<MoreControlsInnerItemResponse>?,
    private var mStoreControlStaticText: MoreControlsStaticTextResponse?
) : RecyclerView.Adapter<MoreControlsInnerItemAdapter.MoreControlsItemViewHolder>() {

    inner class MoreControlsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)
        val valueTextView: TextView = itemView.findViewById(R.id.valueTextView)
        val newTextView: TextView = itemView.findViewById(R.id.newTextView)
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreControlsItemViewHolder = MoreControlsItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.more_controls_inner_item, parent, false))

    override fun getItemCount(): Int = mStoreControlItemsList?.size ?: 0

    override fun onBindViewHolder(holder: MoreControlsItemViewHolder, position: Int) {
        val item = mStoreControlItemsList?.get(position)
        holder.apply {
            headingTextView.text = item?.heading
            setHtmlData(valueTextView, if (isNotEmpty(item?.value)) item?.value else item?.subHeading)
            valueTextView.visibility = if (isEmpty(item?.value) && isEmpty(item?.subHeading)) View.GONE else View.VISIBLE
            newTextView.visibility = if (true == item?.isNew) {
                newTextView.text = mStoreControlStaticText?.text_new
                View.VISIBLE
            } else View.GONE
            mContext?.let { context ->
                when {
                    1 == (mStoreControlItemsList?.size ?: 0) -> {
                        val param = container.layoutParams as ViewGroup.MarginLayoutParams
                        param.setMargins(5,15,5,15)
                        container.layoutParams = param
                        container.background = ContextCompat.getDrawable(context, R.drawable.ripple_slight_curve_grey_white_background)
                    }
                    0 == position -> {
                        val param = container.layoutParams as ViewGroup.MarginLayoutParams
                        param.setMargins(5,15,5,0)
                        container.layoutParams = param
                        container.background = ContextCompat.getDrawable(context, R.drawable.ripple_upper_curve_grey_white_background)
                    }
                    ((mStoreControlItemsList?.size ?: 0) - 1) == position -> {
                        val param = container.layoutParams as ViewGroup.MarginLayoutParams
                        param.setMargins(5,1,5,15)
                        container.layoutParams = param
                        container.background = ContextCompat.getDrawable(context, R.drawable.ripple_lower_curve_grey_white_background)
                    }
                    else -> {
                        val param = container.layoutParams as ViewGroup.MarginLayoutParams
                        param.setMargins(5,1,5,0)
                        container.layoutParams = param
                        container.background = ContextCompat.getDrawable(context, R.drawable.ripple_rect_grey_white_background)
                    }
                }
            }
        }
    }

}