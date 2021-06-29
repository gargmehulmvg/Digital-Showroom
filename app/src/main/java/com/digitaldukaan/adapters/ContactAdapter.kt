package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IContactItemClicked
import com.digitaldukaan.models.dto.ContactModel
import java.util.*

class ContactAdapter(
    private var mList: ArrayList<ContactModel>,
    private var mContext: MainActivity?,
    private var mListener: IContactItemClicked?
) : RecyclerView.Adapter<ContactAdapter.ImagesSearchViewHolder>() {

    inner class ImagesSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val searchImageView: TextView = itemView.findViewById(R.id.searchImageView)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val container: View = itemView.findViewById(R.id.container)
    }

    fun setContactList(list: ArrayList<ContactModel>) {
        this.mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesSearchViewHolder {
        val view = ImagesSearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        )
        view.container.setOnClickListener {
            mListener?.onContactItemClicked(mList[view.adapterPosition])
        }
        return view
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ImagesSearchViewHolder, position: Int) {
        holder.apply {
            val item = mList[position]
            val name = item.name
            if (item.number?.length != mContext?.resources?.getInteger(R.integer.mobile_number_length)) {
                container.isEnabled = false
                container.alpha = 0.2f
            } else {
                container.isEnabled = true
                container.alpha = 1f
            }
            val initials: String
            initials = when {
                name?.length == 1 -> "${name.toCharArray()[0]}#"
                Character.isDigit(name?.toCharArray()?.get(0) ?: '0') -> "#"
                else -> "${name?.toCharArray()?.get(0)}${name?.toCharArray()?.get(1)}"
            }
            textView.text = name
            searchImageView.text = initials.toUpperCase(Locale.getDefault())
        }
    }

}