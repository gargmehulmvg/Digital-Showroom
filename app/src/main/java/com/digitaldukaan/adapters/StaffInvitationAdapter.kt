package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.InvitationListItem

class StaffInvitationAdapter(
    private var mItemList: ArrayList<InvitationListItem?>?,
    private var mListener: IAdapterItemClickListener
    ) : RecyclerView.Adapter<StaffInvitationAdapter.MultiUserSelectionViewHolder>() {

    private var mIsAllItemExpanded = false

    inner class MultiUserSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
        val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)
        val subHeadingTextView: TextView = itemView.findViewById(R.id.subHeadingTextView)
        val radioButtonContainer: ConstraintLayout = itemView.findViewById(R.id.radioButtonContainer)
    }

    fun showCompleteList() {
        this.mIsAllItemExpanded = true
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiUserSelectionViewHolder {
        val view = MultiUserSelectionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.multi_user_selection_item, parent, false))
        view.radioButtonContainer.setOnClickListener { mListener.onAdapterItemClickListener(view.adapterPosition) }
        return view
    }

    override fun getItemCount(): Int = if (mIsAllItemExpanded) (mItemList?.size ?: 0) else 1

    override fun onBindViewHolder(holder: MultiUserSelectionViewHolder, position: Int) {
        val item = mItemList?.get(position)
        holder.apply {
            headingTextView.text = item?.heading
            subHeadingTextView.apply {
                visibility = if (isNotEmpty(item?.subHeading)) {
                    text = item?.subHeading
                    View.VISIBLE
                } else View.GONE
            }
            radioButton.isChecked = item?.isSelected ?: false
            radioButtonContainer.alpha = if (true == item?.isSelected) 1f else 0.3f
        }
    }

}