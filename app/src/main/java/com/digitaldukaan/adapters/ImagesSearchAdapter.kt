package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.ISearchImageItemClicked
import com.squareup.picasso.Picasso

class ImagesSearchAdapter :
    RecyclerView.Adapter<ImagesSearchAdapter.ImagesSearchViewHolder>() {

    private var mSearchImagesList: ArrayList<String> = ArrayList()
    private lateinit var mListener: ISearchImageItemClicked

    fun setSearchImageList(list:ArrayList<String>) {
        this.mSearchImagesList = list
        notifyDataSetChanged()
    }

    fun setSearchImageListener(listener:ISearchImageItemClicked) {
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
            mListener.onSearchImageItemClicked(mSearchImagesList[view.adapterPosition])
        }
        return view
    }

    override fun getItemCount(): Int = mSearchImagesList.size

    override fun onBindViewHolder(
        holder: ImagesSearchViewHolder,
        position: Int
    ) {
        holder.searchImageView.apply {
            Picasso.get().load(mSearchImagesList[position]).into(this)
        }
    }

}