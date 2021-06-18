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
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.ITransactionItemClicked
import com.digitaldukaan.models.response.MyPaymentsItemResponse
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter

class TransactionsAdapter(
    private val activity: MainActivity?,
    private var mPaymentList: ArrayList<MyPaymentsItemResponse>? = ArrayList(),
    private var mPaymentMode: String?,
    private var mListener: ITransactionItemClicked?
) : RecyclerView.Adapter<TransactionsAdapter.TransactionsViewHolder>(),
    StickyRecyclerHeadersAdapter<TransactionsAdapter.HeaderViewHolder> {

    inner class TransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.container)
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        val subHeadingTextView: TextView = itemView.findViewById(R.id.subHeadingTextView)
        val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val smallImageView: ImageView = itemView.findViewById(R.id.smallImageView)
        val tickImageView: ImageView = itemView.findViewById(R.id.tickImageView)
    }

    fun setMyPaymentsList(orderList: ArrayList<MyPaymentsItemResponse>?) {
        this.mPaymentList = orderList
        this.notifyDataSetChanged()
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.headerTitle)
    }


    override fun getHeaderId(position: Int): Long {
        val item = mPaymentList?.get(position)
        return item?.updatedDate?.time ?: 0
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): HeaderViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.my_payments_date_sticky_header_layout, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder?, position: Int) {
        holder?.apply {
            val item = mPaymentList?.get(position)
            headerTextView.text = item?.updatedDate?.let { getStringFromOrderDate(it) }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val view = TransactionsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transactions_item, parent, false))
        view.container.setOnClickListener {
            mListener?.onTransactionItemClicked(mPaymentList?.get(view.adapterPosition)?.transactionId)
        }
        return view
    }

    override fun getItemCount(): Int = mPaymentList?.size ?: 0

    override fun onBindViewHolder(holder: TransactionsAdapter.TransactionsViewHolder, position: Int) {
        holder.apply {
            val item = mPaymentList?.get(position)
            headingTextView.text = if (!isEmpty(item?.utr)) "UTR : ${item?.utr}" else "Order No. ${item?.orderId}"
            val status = "${item?.transactionState} | ${getStringTimeFromDate(getCompleteDateFromOrderString(item?.transactionTimestamp))}"
            subHeadingTextView.text = status
            val amount = "${activity?.getString(R.string.rupee_symbol)} ${item?.amount}"
            amountTextView.text = amount
            if (Constants.MODE_SETTLEMENTS == mPaymentMode) {
                tickImageView.visibility = View.GONE
                imageView.visibility = View.GONE
                smallImageView.visibility = View.VISIBLE
                if (!isEmpty(item?.imageUrl)) activity?.let { context -> Glide.with(context).load(item?.imageUrl).into(smallImageView) }
            } else {
                tickImageView.visibility = View.VISIBLE
                imageView.visibility = View.VISIBLE
                smallImageView.visibility = View.GONE
                if (!isEmpty(item?.imageUrl)) activity?.let { context -> Glide.with(context).load(item?.imageUrl).into(imageView) }
                activity?.let { context ->
                    if (Constants.MODE_SETTLED.equals(item?.transactionState, true)) {
                        Glide.with(context).load(R.drawable.ic_tick_green_small).into(tickImageView)
                    } else Glide.with(context).load(R.drawable.ic_unsettle_transactions).into(tickImageView)
                }
            }
        }
    }

}