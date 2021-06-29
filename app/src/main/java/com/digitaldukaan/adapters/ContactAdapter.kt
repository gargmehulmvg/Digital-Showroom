package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.interfaces.ISearchItemClicked
import java.util.*

class ContactAdapter(private var mContext: Context?, private var mListener: ISearchItemClicked?) : RecyclerView.Adapter<ContactAdapter.ImagesSearchViewHolder>() {

    inner class ImagesSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val searchImageView: TextView = itemView.findViewById(R.id.searchImageView)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val container: View = itemView.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesSearchViewHolder {
        val view = ImagesSearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        )
        view.container.setOnClickListener {
            mListener?.onSearchImageItemClicked(view.adapterPosition.toString())
        }
        return view
    }

    override fun getItemCount(): Int = StaticInstances.sUserContactList.size

    override fun onBindViewHolder(holder: ImagesSearchViewHolder, position: Int) {
        holder.apply {
            val item = StaticInstances.sUserContactList[position]
            val name = item.name
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