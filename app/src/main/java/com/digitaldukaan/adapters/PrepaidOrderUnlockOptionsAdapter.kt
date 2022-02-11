package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.UnlockOptionItemList

class PrepaidOrderUnlockOptionsAdapter(
    private var mHowItGoesList: ArrayList<UnlockOptionItemList>?,
    private var mContext: Context?,
    private var mListener: IAdapterItemClickListener?
) :
    RecyclerView.Adapter<PrepaidOrderUnlockOptionsAdapter.PrepaidOrderUnlockOptionsViewHolder>() {

    inner class PrepaidOrderUnlockOptionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val completeImageView: TextView = itemView.findViewById(R.id.completeImageView)
        val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)
        val subHeadingTextView: TextView = itemView.findViewById(R.id.subHeadingTextView)
        val imageView: TextView = itemView.findViewById(R.id.completeImageView)
        val forwardImageView: View = itemView.findViewById(R.id.forwardImageView)
        val container: View = itemView.findViewById(R.id.container)
        val bottomSheetContainer: CardView = itemView.findViewById(R.id.bottomSheetContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrepaidOrderUnlockOptionsViewHolder {
        val view = PrepaidOrderUnlockOptionsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.prepaid_order_unlock_options_item, parent, false)
        )
        view.container.setOnClickListener {
            val position = view.adapterPosition
            val item = mHowItGoesList?.get(position)
            if (item?.isEditable == true) mListener?.onAdapterItemClickListener(position)
        }
        return view
    }

    override fun getItemCount(): Int = mHowItGoesList?.size ?: 0

    override fun onBindViewHolder(
        holder: PrepaidOrderUnlockOptionsViewHolder,
        position: Int
    ) {
        holder.apply {
            val item = mHowItGoesList?.get(position)
            headingTextView.text = item?.heading
            subHeadingTextView.text = item?.subHeading
            if (item?.isEditable == true) {
                bottomSheetContainer.cardElevation = 10f
                val countStr = "${position + 1}"
                completeImageView.text = countStr
                forwardImageView.visibility = View.VISIBLE
                mContext?.let {
                    completeImageView.background = ContextCompat.getDrawable(it, R.drawable.blue_ring)
                    headingTextView.setTextColor(ContextCompat.getColor(it, R.color.black))
                }
            } else {
                bottomSheetContainer.cardElevation = 2f
                completeImageView.text = null
                forwardImageView.visibility = View.INVISIBLE
                mContext?.let {
                    completeImageView.background = ContextCompat.getDrawable(it, R.drawable.ic_order_detail_blue_tick)
                    headingTextView.setTextColor(ContextCompat.getColor(it, R.color.primary_blue))
                    subHeadingTextView.setTextColor(ContextCompat.getColor(it, R.color.dark_blackish))
                }
            }
        }
    }

}