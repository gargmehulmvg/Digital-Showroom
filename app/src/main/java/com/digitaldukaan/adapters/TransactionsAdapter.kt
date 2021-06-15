package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R

class TransactionsAdapter(private val activity: MainActivity?) : RecyclerView.Adapter<TransactionsAdapter.TransactionsViewHolder>() {

    inner class TransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        return TransactionsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transactions_item, parent, false))
    }

    override fun getItemCount(): Int = 15

    override fun onBindViewHolder(holder: TransactionsAdapter.TransactionsViewHolder, position: Int) {

    }

}