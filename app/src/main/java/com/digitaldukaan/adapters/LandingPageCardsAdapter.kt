package com.digitaldukaan.adapters

import android.graphics.Color
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.ILandingPageAdapterListener
import com.digitaldukaan.models.response.PrimaryDomainItemResponse
import com.digitaldukaan.models.response.ZeroOrderItemsResponse
import java.util.*

class LandingPageCardsAdapter(
    private val mItemList: ArrayList<ZeroOrderItemsResponse?>?,
    private val mFragment: BaseFragment?,
    private val mListener: ILandingPageAdapterListener?
) : RecyclerView.Adapter<LandingPageCardsAdapter.LandingPageCardsViewHolder>() {

    companion object {
        private const val TAG = "LandingPageCardsAdapter"
    }

    inner class LandingPageCardsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardLayout: View = itemView.findViewById(R.id.cardLayout)
        val ctaContainer: ConstraintLayout = itemView.findViewById(R.id.ctaContainer)
        val ctaMarginContainer: ConstraintLayout = itemView.findViewById(R.id.ctaMarginContainer)
        val completedLayout: ConstraintLayout = itemView.findViewById(R.id.completedLayout)
        val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)
        val subHeadingTextView: TextView = itemView.findViewById(R.id.subHeadingTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val ctaTextView: TextView = itemView.findViewById(R.id.ctaTextView)
        val themeTextView1: TextView = itemView.findViewById(R.id.themeTextView1)
        val themeTextView2: TextView = itemView.findViewById(R.id.themeTextView2)
        val completedHeadingTextView: TextView = itemView.findViewById(R.id.completedHeadingTextView)
        val completedSubHeadingTextView: TextView = itemView.findViewById(R.id.completedSubHeadingTextView)
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
        val progressBar: View = itemView.findViewById(R.id.progressBar)
        val ctaArrowImageView: ImageView = itemView.findViewById(R.id.ctaArrowImageView)
        val heroBannerImageView: ImageView = itemView.findViewById(R.id.heroBannerImageView)
        val containerBackGround: ImageView = itemView.findViewById(R.id.containerBackGround)
        val ctaBackgroundImageView: ImageView = itemView.findViewById(R.id.backgroundImageView)
        val themeRightImageView: ImageView = itemView.findViewById(R.id.themeRightImageView)
        val themeViewContainer: View = itemView.findViewById(R.id.themeViewContainer)
        val messageRecyclerView: RecyclerView = itemView.findViewById(R.id.messageRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LandingPageCardsViewHolder =
        LandingPageCardsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.landing_page_cards_item, parent, false))

    override fun getItemCount(): Int = mItemList?.size ?: 0

    override fun onBindViewHolder(holder: LandingPageCardsViewHolder, position: Int) {
        val item = mItemList?.get(position)
        holder.apply {
            if (true == item?.completed?.isCompleted) {
                cardLayout.visibility = View.GONE
                completedLayout.visibility = View.VISIBLE
                completedHeadingTextView.text = item.completed?.heading
                completedSubHeadingTextView.text = item.completed?.subHeading
            } else {
                cardLayout.visibility = View.VISIBLE
                completedLayout.visibility = View.GONE
                if (isNotEmpty(item?.bgColor)) {
                    containerBackGround.visibility = View.GONE
                    container.setBackgroundColor(Color.parseColor(item?.bgColor))
                } else {
                    containerBackGround.visibility = View.VISIBLE
                    mFragment?.let { context -> Glide.with(context).load(item?.bgImage).into(containerBackGround) }
                }
                headingTextView.apply {
                    text = item?.heading
                    setTextColor(if (isNotEmpty(item?.headingTextColor)) Color.parseColor(item?.headingTextColor) else Color.WHITE)
                }
                messageTextView.apply {
                    if (Constants.KEY_BUY_THEMES == item?.id) {
                        themeViewContainer.visibility = View.VISIBLE
                        if (isNotEmpty(item.heroImage) && item.isPrimary) {
                            themeRightImageView.visibility = View.GONE
                            heroBannerImageView.visibility = View.VISIBLE
                            mFragment?.let { context -> Glide.with(context).load(item.heroImage).into(heroBannerImageView) }
                            themeTextView1.textSize = 22f
                            themeTextView2.textSize = 22f
                        } else {
                            themeTextView1.textSize = 16f
                            themeTextView2.textSize = 16f
                            themeRightImageView.visibility = View.VISIBLE
                            heroBannerImageView.visibility = View.GONE
                            mFragment?.let { context -> Glide.with(context).load(item.heroImage).into(themeRightImageView) }
                        }
                        themeTextView1.text = Html.fromHtml(item.message, Html.FROM_HTML_MODE_COMPACT)
                        themeTextView2.text = Html.fromHtml(if (isNotEmpty(item.messageRemainingAndroid)) item.messageRemainingAndroid else "", Html.FROM_HTML_MODE_COMPACT)
                        visibility = View.GONE
                    } else {
                        themeViewContainer.visibility = View.GONE
                        text = Html.fromHtml(item?.message, Html.FROM_HTML_MODE_COMPACT)
                        setTextColor(if (isNotEmpty(item?.messageTextColor)) Color.parseColor(item?.messageTextColor) else Color.WHITE)
                    }
                }
                subHeadingTextView.apply {
                    text = item?.subHeading
                    setTextColor(if (isNotEmpty(item?.subHeadingTextColor)) Color.parseColor(item?.subHeadingTextColor) else Color.WHITE)
                }
                ctaTextView.apply {
                    text = item?.ctaResponse?.text
                    setTextColor(if (isNotEmpty(item?.ctaResponse?.textColor)) Color.parseColor(item?.ctaResponse?.textColor) else Color.WHITE)
                    ctaContainer.setBackgroundColor(if (isNotEmpty(item?.ctaResponse?.bgColor)) Color.parseColor(item?.ctaResponse?.bgColor) else Color.WHITE)
                    if (Constants.KEY_BUY_THEMES == item?.id) {
                        ctaBackgroundImageView.visibility = View.VISIBLE
                        ctaArrowImageView.setImageDrawable(mFragment?.context?.let { context -> ContextCompat.getDrawable(context, R.drawable.ic_half_arrow_forward_small_black) })
                        ctaBackgroundImageView.background = mFragment?.context?.let { context -> ContextCompat.getDrawable(context, R.drawable.ic_theme_bg) }
                        ctaMarginContainer.setBackgroundColor(Color.BLACK)
                    } else {
                        ctaMarginContainer.setBackgroundColor(Color.WHITE)
                        ctaArrowImageView.setImageDrawable(mFragment?.context?.let { context -> ContextCompat.getDrawable(context, R.drawable.ic_half_arrow_forward_small_white) })
                        ctaBackgroundImageView.visibility = View.GONE
                    }
                }
                ctaMarginContainer.setOnClickListener {
                    mListener?.onLandingPageAdapterCtaClicked(item)
                }
                if (true == item?.isPrimary) {
                    setupRecyclerView(messageRecyclerView, progressBar, item)
                    mListener?.onLandingPageAdapterIsPrimaryDetected(position, item)
                }
            }
        }
    }

    private fun setupRecyclerView(messageRecyclerView: RecyclerView, progressBar:View ,zeroOrderItemsResponse: ZeroOrderItemsResponse?) {
        when(zeroOrderItemsResponse?.id) {
            Constants.KEY_BUY_DOMAIN -> {
                if (isEmpty(StaticInstances.sSuggestedDomainsList)) {
                    progressBar.visibility = View.VISIBLE
                    messageRecyclerView.visibility = View.GONE
                } else {
                    progressBar.visibility = View.GONE
                    messageRecyclerView.visibility = View.VISIBLE
                }
                messageRecyclerView.apply {
                    layoutManager = LinearLayoutManager(mFragment?.context, LinearLayoutManager.HORIZONTAL, false)
                    zeroOrderItemsResponse.suggestedDomainsList?.add(PrimaryDomainItemResponse("", "","","","", "","",null,null))
                    adapter = LandingPageCustomDomainAdapter(mFragment, StaticInstances.sSuggestedDomainsList, object : ILandingPageAdapterListener {

                        override fun onLandingPageAdapterIsPrimaryDetected(position: Int, item: ZeroOrderItemsResponse?) = Unit

                        override fun onLandingPageAdapterCustomDomainApplyItemClicked(item: PrimaryDomainItemResponse?) {
                            mListener?.onLandingPageAdapterCustomDomainApplyItemClicked(item)
                        }

                        override fun onLandingPageAdapterAddProductItemClicked() = Unit

                        override fun onLandingPageAdapterCtaClicked(item: ZeroOrderItemsResponse?) = Unit

                        override fun onLandingPageCustomCtaClicked(url: String?) {
                            mListener?.onLandingPageCustomCtaClicked(url)
                        }

                    })
                }
                try {
                    val helper = PagerSnapHelper()
                    helper.attachToRecyclerView(messageRecyclerView)
                } catch (e: Exception) {
                    Log.e(TAG, "setupRecyclerView: do nothing", e)
                }
            }
            Constants.KEY_ADD_PRODUCT -> {
                messageRecyclerView.apply {
                    visibility = View.VISIBLE
                    layoutManager = LinearLayoutManager(mFragment?.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = LandingPageAddProductsAdapter(mFragment?.context, zeroOrderItemsResponse.messageListData, object : IAdapterItemClickListener {

                        override fun onAdapterItemClickListener(position: Int) {
                            mListener?.onLandingPageAdapterAddProductItemClicked()
                        }

                    })
                }
            }
        }
    }

    fun setItemToPosition(position: Int, item: ZeroOrderItemsResponse?) {
        mItemList?.set(position, item)
        notifyItemChanged(position)
    }

}