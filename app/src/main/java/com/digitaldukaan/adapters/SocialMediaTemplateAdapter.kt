package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.interfaces.ISocialMediaTemplateItemClickListener
import com.digitaldukaan.models.response.SocialMediaTemplateListItemResponse

class SocialMediaTemplateAdapter(
    private var mContext: BaseFragment?,
    private var mList: ArrayList<SocialMediaTemplateListItemResponse?>?,
    private var mListener: ISocialMediaTemplateItemClickListener?
) :
    RecyclerView.Adapter<SocialMediaTemplateAdapter.SocialMediaTemplateViewHolder>() {

    inner class SocialMediaTemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val favouriteTextView: TextView = itemView.findViewById(R.id.favouriteTextView)
        val editTextView: TextView = itemView.findViewById(R.id.editTextView)
        val shareTextView: TextView = itemView.findViewById(R.id.shareTextView)
        val whatsappTextView: TextView = itemView.findViewById(R.id.whatsappTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val container: View = itemView.findViewById(R.id.container)
    }

    fun setListToAdapter(list: ArrayList<SocialMediaTemplateListItemResponse?>?) {
        this.mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialMediaTemplateViewHolder {
        val view = SocialMediaTemplateViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.social_media_template_item_layout, parent, false))
        view.favouriteTextView.setOnClickListener {
            mContext?.startViewAnimation(view.favouriteTextView)
            mListener?.onSocialMediaTemplateFavItemClickListener(view.adapterPosition, mList?.get(view.adapterPosition))
        }
        view.shareTextView.setOnClickListener { mListener?.onSocialMediaTemplateShareItemClickListener(view.adapterPosition, mList?.get(view.adapterPosition)) }
        view.editTextView.setOnClickListener { mListener?.onSocialMediaTemplateEditItemClickListener(view.adapterPosition, mList?.get(view.adapterPosition)) }
        view.whatsappTextView.setOnClickListener { mListener?.onSocialMediaTemplateWhatsappItemClickListener(view.adapterPosition, mList?.get(view.adapterPosition)) }
        return view
    }

    override fun getItemCount(): Int = mList?.size ?: 0

    override fun onBindViewHolder(holder: SocialMediaTemplateViewHolder, position: Int) {
        holder.apply {
            val item = mList?.get(position)
            if (true == item?.favourite?.isActive) {
                favouriteTextView.visibility = View.VISIBLE
                favouriteTextView.text = item.favourite?.text
            } else favouriteTextView.visibility = View.GONE
            if (true == item?.edit?.isActive) {
                editTextView.visibility = View.VISIBLE
                editTextView.text = item.edit?.text
            } else editTextView.visibility = View.GONE
            if (true == item?.share?.isActive) {
                shareTextView.visibility = View.VISIBLE
                shareTextView.text = item.share?.text
            } else shareTextView.visibility = View.GONE
            if (true == item?.whatsapp?.isActive) {
                whatsappTextView.visibility = View.VISIBLE
                whatsappTextView.text = item.whatsapp?.text
            } else whatsappTextView.visibility = View.GONE
            favouriteTextView.setCompoundDrawablesWithIntrinsicBounds(0, if (true == item?.isFavourite) R.drawable.ic_favourite else R.drawable.ic_un_favourite, 0, 0)
            mContext?.let { context ->
                Glide.with(context)
                    .load(item?.coverImage)
                    .dontAnimate()
                    .dontTransform()
                    .override(350,350)
                    .into(imageView)
            }
        }
    }

}