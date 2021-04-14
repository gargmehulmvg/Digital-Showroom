package com.digitaldukaan.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.EditPremiumColorItemResponse

class EditPremiumColorAdapter(
    private var mColorList: ArrayList<EditPremiumColorItemResponse?>?,
    private var mListener: IAdapterItemClickListener,
    private var mCurrentThemeText: String?
) :
    RecyclerView.Adapter<EditPremiumColorAdapter.MarketingCardViewHolder>() {

    fun setEditPremiumColorList(colorList: ArrayList<EditPremiumColorItemResponse?>?) {
        this.mColorList = colorList
        notifyDataSetChanged()
    }

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val currentSelectionTextView: TextView = itemView.findViewById(R.id.currentSelectionTextView)
        val itemContainer: View = itemView.findViewById(R.id.itemContainer)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val tickImageView: ImageView = itemView.findViewById(R.id.tickImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        val view = MarketingCardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_edit_premium_color_item, parent, false)
        )
        view.itemContainer.setOnClickListener {
            mListener.onAdapterItemClickListener(view.adapterPosition)
        }
        return view
    }

    override fun getItemCount(): Int = mColorList?.size ?: 0

    override fun onBindViewHolder(
        holder: MarketingCardViewHolder,
        position: Int
    ) {
        val item = mColorList?.get(position)
        holder.run {
            imageView.setBackgroundColor(Color.parseColor(item?.primaryColor))
            if (position == 0) {
                currentSelectionTextView.text = mCurrentThemeText
            }
            tickImageView.visibility = if (item?.isSelected == true) View.VISIBLE else View.GONE
        }
    }

}