package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.CommonCtaResponse

class LandingPageShortcutsAdapter(
    private val mContext: Context?,
    private val mShortcutsList: ArrayList<CommonCtaResponse?>?,
    private val mListener: IAdapterItemClickListener?
) : RecyclerView.Adapter<LandingPageShortcutsAdapter.AddBankBottomSheetViewHolder>() {

    inner class AddBankBottomSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.container)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddBankBottomSheetViewHolder {
        val view = AddBankBottomSheetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.landing_page_shortcut_item, parent, false))
        view.container.setOnClickListener {
            mListener?.onAdapterItemClickListener(view.adapterPosition)
        }
        return view
    }

    override fun getItemCount(): Int = mShortcutsList?.size ?: 0

    override fun onBindViewHolder(holder: AddBankBottomSheetViewHolder, position: Int) {
        val item = mShortcutsList?.get(position)
        holder.apply {
            textView.text = item?.text
            mContext?.let { context -> Glide.with(context).load(item?.url).into(imageView) }
        }
    }

}