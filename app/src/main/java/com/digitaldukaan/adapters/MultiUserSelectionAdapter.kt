package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IAdapterItemClickListener

class MultiUserSelectionAdapter(
    private var mItemList: ArrayList<String>,
    private var mListener: IAdapterItemClickListener
    ) : RecyclerView.Adapter<MultiUserSelectionAdapter.MultiUserSelectionViewHolder>() {

    private var mIsAllItemExpanded = false

    inner class MultiUserSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
        val radioButtonContainer: LinearLayout = itemView.findViewById(R.id.radioButtonContainer)
    }

    fun showCompleteList() {
        this.mIsAllItemExpanded = true
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiUserSelectionViewHolder {
        val view = MultiUserSelectionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.multi_user_selection_item, parent, false))

        return view
    }

    override fun getItemCount(): Int = if (mIsAllItemExpanded) mItemList.size else 1

    override fun onBindViewHolder(holder: MultiUserSelectionViewHolder, position: Int) {
        val item = mItemList[position]
        holder.radioButton.text = item
        holder.radioButtonContainer.apply {
            alpha = if (0 != position) 0.3f else 1f
        }
    }

}