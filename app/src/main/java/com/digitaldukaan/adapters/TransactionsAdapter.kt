package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.constants.getCompleteDateFromOrderString
import com.digitaldukaan.constants.getStringTimeFromDate
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.models.response.MyPaymentsItemResponse

class TransactionsAdapter(private val activity: MainActivity?, private var mPaymentList: ArrayList<MyPaymentsItemResponse>? = ArrayList()) : RecyclerView.Adapter<TransactionsAdapter.TransactionsViewHolder>() {

    inner class TransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        val subHeadingTextView: TextView = itemView.findViewById(R.id.subHeadingTextView)
        val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        return TransactionsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transactions_item, parent, false))
    }

    override fun getItemCount(): Int = mPaymentList?.size ?: 0

    override fun onBindViewHolder(holder: TransactionsAdapter.TransactionsViewHolder, position: Int) {
        holder.apply {
            val item = mPaymentList?.get(position)
            if (!isEmpty(item?.imageUrl)) activity?.let { context -> Glide.with(context).load(item?.imageUrl).into(imageView) }
            headingTextView.text = if (isEmpty(item?.orderId)) "UTR : ${item?.utr}" else "Order No. ${item?.orderId}"
            val status = "${item?.transactionState} | ${getStringTimeFromDate(getCompleteDateFromOrderString(item?.transactionTimestamp))}"
            subHeadingTextView.text = status
            val amount = "${activity?.getString(R.string.rupee_symbol)} ${item?.amount}"
            amountTextView.text = amount
        }
    }

}