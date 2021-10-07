package com.digitaldukaan.adapters

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.PrimaryDomainItemResponse

class CustomDomainSelectionAdapter(
    private var suggestedDomainsList: ArrayList<PrimaryDomainItemResponse?>?,
    private var listener: IAdapterItemClickListener
) :
    RecyclerView.Adapter<CustomDomainSelectionAdapter.CustomDomainSelectionViewHolder>() {

    inner class CustomDomainSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.container)
        val domainTextView: TextView = itemView.findViewById(R.id.domainTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val promoCodeTextView: TextView = itemView.findViewById(R.id.promoCodeTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val message2TextView: TextView = itemView.findViewById(R.id.message2TextView)
        val buyNowTextView: TextView = itemView.findViewById(R.id.buyNowTextView)
        val originalPriceTextView: TextView = itemView.findViewById(R.id.originalPriceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomDomainSelectionViewHolder {
        val view = CustomDomainSelectionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.custom_domain_selection_item, parent, false))
        view.buyNowTextView.setOnClickListener {
            val position = view.adapterPosition
            if (position < 0 || position > (suggestedDomainsList?.size ?: 0)) return@setOnClickListener
            listener.onAdapterItemClickListener(position)
        }
        return view
    }

    override fun getItemCount(): Int = suggestedDomainsList?.size ?: 0

    override fun onBindViewHolder(holder: CustomDomainSelectionViewHolder, position: Int) {
        val item = suggestedDomainsList?.get(position)
        holder.apply {
            Log.d("CustomDomainSelectionAdapter", "onBindViewHolder: $position :: $item")
            domainTextView.text = item?.domainName
            messageTextView.text = item?.info_data?.firstYearText?.trim()
            message2TextView.text = item?.info_data?.renewsText?.trim()
            promoCodeTextView.text = item?.promo
            buyNowTextView.apply {
                text = item?.cta?.text
                setTextColor(Color.parseColor(item?.cta?.textColor))
            }
            priceTextView.text = "₹${item?.discountedPrice}"
            originalPriceTextView.apply {
                text = "₹${item?.originalPrice}"
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }
    }

}