package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.squareup.picasso.Picasso

class AddProductsImagesAdapter(
    private var mImagesList: ArrayList<String>?,
    private var mNoImagesText: String?
) :
    RecyclerView.Adapter<AddProductsImagesAdapter.AddProductsImagesViewHolder>() {

    inner class AddProductsImagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noImagesLayout: View = itemView.findViewById(R.id.noImagesLayout)
        val image: ImageView = itemView.findViewById(R.id.image)
        val updateCameraTextView: TextView = itemView.findViewById(R.id.updateCameraTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddProductsImagesViewHolder {
        return AddProductsImagesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.add_product_images_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mImagesList?.size ?: 0

    override fun onBindViewHolder(
        holder: AddProductsImagesViewHolder,
        position: Int
    ) {
        holder.apply {
            if (position == 0) {
                noImagesLayout.visibility = View.VISIBLE
                image.visibility = View.GONE
                updateCameraTextView.text = mNoImagesText
            } else {
                noImagesLayout.visibility = View.GONE
                image.visibility = View.VISIBLE
                Picasso.get().load(mImagesList?.get(position)).into(image)
            }
        }
    }

}