package com.digitaldukaan.adapters

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.interfaces.ILandingPageAdapterListener
import com.digitaldukaan.models.response.PrimaryDomainItemResponse
import com.skydoves.balloon.showAlignTop

class LandingPageCustomDomainAdapter(
    private val mFragment: BaseFragment?,
    private var mSuggestedDomainsList: ArrayList<PrimaryDomainItemResponse>?,
    private var mListenerNew: ILandingPageAdapterListener?
) :
    RecyclerView.Adapter<LandingPageCustomDomainAdapter.CustomDomainSelectionViewHolder>() {

    inner class CustomDomainSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.container)
        val separator: View = itemView.findViewById(R.id.separator2)
        val lastViewContainer: View = itemView.findViewById(R.id.lastViewContainer)
        val domainTextView: TextView = itemView.findViewById(R.id.domainTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val promoCodeTextView: TextView = itemView.findViewById(R.id.promoCodeTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val buyNowTextView: TextView = itemView.findViewById(R.id.buyNowTextView)
        val searchDomainContainer: View = itemView.findViewById(R.id.searchDomainContainer)
        val alreadyHaveADomainTextView: View = itemView.findViewById(R.id.alreadyHaveADomainTextView)
        val originalPriceTextView: TextView = itemView.findViewById(R.id.originalPriceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomDomainSelectionViewHolder {
        val view = CustomDomainSelectionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.landing_page_custom_domain_item, parent, false))
        view.buyNowTextView.setOnClickListener {
            val position = view.adapterPosition
            if (position < 0 || position > (mSuggestedDomainsList?.size ?: 0)) return@setOnClickListener
            mListenerNew?.onLandingPageAdapterCustomDomainApplyItemClicked(mSuggestedDomainsList?.get(position))
        }
        return view
    }

    override fun getItemCount(): Int = mSuggestedDomainsList?.size ?: 0

    override fun onBindViewHolder(holder: CustomDomainSelectionViewHolder, position: Int) {
        val item = mSuggestedDomainsList?.get(position)
        holder.apply {
            Log.d("CustomDomainSelectionAdapter", "onBindViewHolder: $position :: $item")
            if (position == ((mSuggestedDomainsList?.size ?: 0) - 1)) {
                lastViewContainer.visibility = View.VISIBLE
                separator.visibility = View.VISIBLE
                searchDomainContainer.setOnClickListener {
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_DOMAIN_SEARCH,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                            AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.LANDING_PAGE)
                    )
                    val url = "${StaticInstances.sSearchDomainUrl}?storeid=${PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)}&token=${PrefsManager.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.LANDING_PAGE}"
                    mListenerNew?.onLandingPageCustomCtaClicked(url)
                }
                alreadyHaveADomainTextView.setOnClickListener {
                    val url = "${StaticInstances.sExploreDomainUrl}?storeid=${PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)}&token=${PrefsManager.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.LANDING_PAGE}"
                    mListenerNew?.onLandingPageCustomCtaClicked(url)
                }
            } else {
                separator.visibility = View.GONE
                lastViewContainer.visibility = View.GONE
                domainTextView.text = item?.domainName
                val infoStr = item?.infoData?.firstYearText?.trim() + "\n" + item?.infoData?.renewsText?.trim()
                messageTextView.text = item?.validity
                promoCodeTextView.text = item?.promo
                buyNowTextView.apply {
                    text = item?.cta?.text
                    setTextColor(if (isNotEmpty(item?.cta?.textColor)) Color.parseColor(item?.cta?.textColor) else Color.WHITE)
                }
                var priceStr = "₹${item?.discountedPrice}"
                priceTextView.text = priceStr
                originalPriceTextView.apply {
                    priceStr = "₹${item?.originalPrice}"
                    text = priceStr
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                messageTextView.setOnClickListener {
                    val toolTipBalloon = getToolTipBalloon(mFragment?.context, infoStr, 0.75f)
                    toolTipBalloon?.let { b -> messageTextView.showAlignTop(b) }
                }
            }
        }
    }

}