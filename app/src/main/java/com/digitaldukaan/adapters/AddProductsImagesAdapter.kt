package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.squareup.picasso.Picasso

class AddProductsImagesAdapter(
    private var mImagesList: ArrayList<String>?,
    private var mNoImagesText: String?,
    private var mAdapterItemClick: IAdapterItemClickListener
) :
    RecyclerView.Adapter<AddProductsImagesAdapter.AddProductsImagesViewHolder>() {

    inner class AddProductsImagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageContainer: View = itemView.findViewById(R.id.imageContainer)
        val noImagesLayout: View = itemView.findViewById(R.id.noImagesLayout)
        val image: ImageView = itemView.findViewById(R.id.image)
        val updateCameraTextView: TextView = itemView.findViewById(R.id.updateCameraTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddProductsImagesViewHolder {
        val holder = AddProductsImagesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.add_product_images_item, parent, false)
        )
        holder.imageContainer.setOnClickListener { mAdapterItemClick.onAdapterItemClickListener(holder.adapterPosition) }
        return holder
    }

    fun setListToAdapter(imagesList: ArrayList<String>?) {
        this.mImagesList = imagesList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mImagesList?.size ?: 0
    }

    override fun onBindViewHolder(
        holder: AddProductsImagesViewHolder,
        position: Int
    ) {
        holder.apply {
            val imageStr = mImagesList?.get(position)
            if (imageStr?.isEmpty() == true) {
                noImagesLayout.visibility = View.VISIBLE
                image.visibility = View.GONE
                updateCameraTextView.text = mNoImagesText
                if ((mImagesList?.isNotEmpty() == true && mImagesList?.size == 5)) {
                    imageContainer.alpha = 0.2f
                    imageContainer.isEnabled = false
                }
            } else {
                noImagesLayout.visibility = View.GONE
                image.visibility = View.VISIBLE
                Picasso.get().load(imageStr).into(image)
            }
        }
    }

}