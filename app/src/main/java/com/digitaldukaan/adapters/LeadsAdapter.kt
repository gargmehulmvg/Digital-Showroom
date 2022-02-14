package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.ILeadsListItemListener
import com.digitaldukaan.models.response.LeadsResponse
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import java.util.*

class LeadsAdapter(
    private var mContext: Context,
    private var mLeadsList: ArrayList<LeadsResponse>?,
    private var mListItemListener: ILeadsListItemListener?
) : RecyclerView.Adapter<LeadsAdapter.LeadsViewHolder>(), StickyRecyclerHeadersAdapter<LeadsAdapter.HeaderViewHolder> {

    fun setLeadsList(leadsList: ArrayList<LeadsResponse>?) {
        this.mLeadsList = leadsList
        this.notifyDataSetChanged()
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.headerTitle)
    }

    override fun getHeaderId(position: Int): Long {
        val item = mLeadsList?.get(position)
        return item?.updatedDate?.time ?: 0
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): HeaderViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.order_date_sticky_header_layout, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder?, position: Int) {
        holder?.apply {
            val item = mLeadsList?.get(position)
            headerTextView.text = item?.updatedDate?.run { getStringFromOrderDate(this) }
        }
    }
    
    inner class LeadsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cartImageView: ImageView = itemView.findViewById(R.id.cartImageView)
        val leadItemContainer: View = itemView.findViewById(R.id.leadItemContainer)
        val leadDetailTextView: TextView = itemView.findViewById(R.id.leadDetailTextView)
        val leadLastUpdatedTextView: TextView = itemView.findViewById(R.id.leadLastUpdatedTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val cartStatusTextView: TextView = itemView.findViewById(R.id.cartStatusTextView)
        val sendOfferTextView: TextView = itemView.findViewById(R.id.sendOfferTextView)
        val notificationSentTextView: TextView = itemView.findViewById(R.id.notificationSentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeadsViewHolder {
        val view = LeadsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.leads_item, parent, false))
        view.leadItemContainer.setOnClickListener {
            mListItemListener?.onLeadsItemCLickedListener(mLeadsList?.get(view.adapterPosition))
        }
        return view
    }

    override fun getItemCount(): Int = mLeadsList?.size ?:0

    override fun onBindViewHolder(holder: LeadsAdapter.LeadsViewHolder, position: Int) {
        val item = mLeadsList?.get(position)
        holder.apply {
            var displayStr: String = if (isEmpty(item?.customerName)) "${item?.phoneNumber}" else "${item?.phoneNumber} | ${item?.customerName}"
            leadDetailTextView.text = displayStr
            displayStr = "₹${item?.orderValue}"
            priceTextView.text = displayStr
            val oldDate = getCompleteDateFromOrderString(item?.lastUpdateOn)
            val currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
            var diff: Long = currentDate.time - (oldDate?.time ?: 0)
            var secondsInMilli: Long = 1000
            var minutesInMilli = secondsInMilli * 60
            var hoursInMilli = minutesInMilli * 60
            var daysInMilli = hoursInMilli * 24
            var days: Long = diff / daysInMilli
            diff %= daysInMilli
            var hours: Long = diff / hoursInMilli
            diff %= hoursInMilli
            var minutes: Long = diff / minutesInMilli
            diff %= minutesInMilli
            var displayStrNew = ""
            if(days!=0L || hours>12){
                displayStrNew = "${StaticInstances.sOrderPageInfoStaticData?.text_cart_updated_on} ${getDateStringForLeadsHeader(getDateFromOrderString(item?.lastUpdateOn) ?: Date())}"
            }   else if(hours!=0L){
                displayStr = "${StaticInstances.sOrderPageInfoStaticData?.text_cart_updated_in_hours}"
                displayStrNew = displayStr.replace("xxx", "$hours", true)
            }  else if(minutes!=0L){
                displayStr = "${StaticInstances.sOrderPageInfoStaticData?.text_cart_updated_in_minutes}"
                displayStrNew = displayStr.replace("xxx", "$minutes", true)
            }   else{
                displayStrNew = "${StaticInstances.sOrderPageInfoStaticData?.text_cart_updated_recently}"
            }
            leadLastUpdatedTextView.text = displayStrNew
            Glide.with(mContext)
                .load(if (Constants.CART_TYPE_ABANDONED == item?.cartType) R.drawable.ic_abandoned_cart else R.drawable.ic_active_cart)
                .into(cartImageView)
            cartStatusTextView.text = if (Constants.CART_TYPE_ABANDONED == item?.cartType) StaticInstances.sOrderPageInfoStaticData?.text_cart_abandoned else StaticInstances.sOrderPageInfoStaticData?.text_cart_active
            when {
                isNotEmpty(item?.notificationSentOn) -> {
                    val notificationOldTime = getCompleteDateFromOrderString(item?.notificationSentOn)
                    diff = currentDate.time - (notificationOldTime?.time ?: 0)
                    secondsInMilli = 1000
                    minutesInMilli = secondsInMilli * 60
                    hoursInMilli = minutesInMilli * 60
                    daysInMilli = hoursInMilli * 24
                    days = diff / daysInMilli
                    diff %= daysInMilli
                    hours = diff / hoursInMilli
                    diff %= hoursInMilli
                    minutes = diff / minutesInMilli
                    diff %= minutesInMilli
                    var displayNotificationString = ""
                    if(days!=0L || hours>12){
                        displayNotificationString = "${StaticInstances.sOrderPageInfoStaticData?.text_notification_sent_on} ${getDateStringForLeadsHeader(getDateFromOrderString(item?.notificationSentOn) ?: Date())}"
                    }   else if(hours!=0L){
                        displayStr = "${StaticInstances.sOrderPageInfoStaticData?.text_notification_sent_in_hours}"
                        displayNotificationString = displayStr.replace("xxx", "$hours", true)
                    }  else if(minutes!=0L){
                        displayStr = "${StaticInstances.sOrderPageInfoStaticData?.text_notification_sent_in_minutes}"
                        displayNotificationString = displayStr.replace("xxx", "$minutes", true)
                    }   else{
                        displayNotificationString = "${StaticInstances.sOrderPageInfoStaticData?.text_notification_sent_recently}"
                    }
                    notificationSentTextView.visibility = View.VISIBLE
                    notificationSentTextView.text = displayNotificationString
                    sendOfferTextView.visibility = View.GONE
                }
                Constants.CART_TYPE_ABANDONED == item?.cartType -> {
                    sendOfferTextView.text = StaticInstances.sOrderPageInfoStaticData?.text_send_reminder
                    sendOfferTextView.visibility = View.VISIBLE
                    notificationSentTextView.visibility = View.GONE
                }
                else -> {
                    sendOfferTextView.visibility = View.GONE
                    notificationSentTextView.visibility = View.GONE
                }
            }
            cartStatusTextView.background = ContextCompat.getDrawable(mContext, if (Constants.CART_TYPE_ABANDONED == item?.cartType) R.drawable.curve_red_cart_abandoned_background else R.drawable.curve_blue_cart_active_background)
            cartStatusTextView.setTextColor(ContextCompat.getColor(mContext, if (Constants.CART_TYPE_ABANDONED == item?.cartType) R.color.leads_cart_abandoned_text_color else R.color.leads_cart_active_text_color))
        }
    }
}