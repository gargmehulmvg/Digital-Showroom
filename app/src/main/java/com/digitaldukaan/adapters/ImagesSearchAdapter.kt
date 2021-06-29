package com.digitaldukaan.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.ISearchItemClicked

class ImagesSearchAdapter : RecyclerView.Adapter<ImagesSearchAdapter.ImagesSearchViewHolder>() {

    private var mSearchImagesList: ArrayList<String>? = ArrayList()
    private lateinit var mListener: ISearchItemClicked
    private var mContext: Context? = null

    fun setSearchImageList(list: ArrayList<String>) {
        this.mSearchImagesList = list
        notifyDataSetChanged()
    }

    fun setContext(context: Context?) {
        this.mContext = context
    }

    fun setSearchImageListener(listener: ISearchItemClicked) {
        this.mListener = listener
    }

    inner class ImagesSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val searchImageView: ImageView = itemView.findViewById(R.id.searchImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesSearchViewHolder {
        val view = ImagesSearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.search_images_item, parent, false)
        )
        view.searchImageView.setOnClickListener {
            val position = view.adapterPosition
            Log.d("ImagesSearchAdapter", "searchImageView.setOnClickListener: :: position ::${view.adapterPosition} && imageList size :: ${mSearchImagesList?.size}")
            if (position < 0) return@setOnClickListener
            if (isEmpty(mSearchImagesList)) return@setOnClickListener
            if (position > mSearchImagesList?.size ?: 0) return@setOnClickListener
            Log.d("ImagesSearchAdapter", "searchImageView.setOnClickListener: :: position clicked${view.adapterPosition}")
            mSearchImagesList?.let { mListener.onSearchImageItemClicked(it[view.adapterPosition]) }
        }
        return view
    }

    override fun getItemCount(): Int = mSearchImagesList?.size ?: 0

    override fun onBindViewHolder(holder: ImagesSearchViewHolder, position: Int) {
        holder.searchImageView.apply {
            try {
                val imageStr = mSearchImagesList?.get(position)
                Log.d("ImagesSearchAdapter", "onBindViewHolder: imageStr :: $imageStr")
                mContext?.let { context -> Glide.with(context).load(imageStr).into(this) }
            } catch (e: Exception) {
                Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
            }
        }
    }

}