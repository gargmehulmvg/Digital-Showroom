package com.digitaldukaan.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.AddProductImagesResponse

class AddProductsImagesAdapter(
    private var mFragment: BaseFragment?,
    private var mImagesList: ArrayList<AddProductImagesResponse>?,
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

    fun setListToAdapter(imagesList: ArrayList<AddProductImagesResponse>?) {
        this.mImagesList = imagesList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mImagesList?.size ?: 0

    override fun onBindViewHolder(holder: AddProductsImagesViewHolder, position: Int) {
        holder.apply {
            val imageStr = mImagesList?.get(position)
            if (isEmpty(imageStr?.imageUrl)) {
                noImagesLayout.visibility = View.VISIBLE
                image.visibility = View.GONE
                updateCameraTextView.text = mNoImagesText
                if ((isNotEmpty(mImagesList) && 5 == mImagesList?.size)) {
                    imageContainer.alpha = 0.2f
                    imageContainer.isEnabled = false
                } else {
                    imageContainer.alpha = 1f
                    imageContainer.isEnabled = true
                }
            } else {
                noImagesLayout.visibility = View.GONE
                image.visibility = View.VISIBLE
                image?.let {
                    try {
                        mFragment?.let { fragment -> Glide.with(fragment).load(imageStr?.imageUrl).into(it) }
                    } catch (e: Exception) {
                        Log.e(AddProductsImagesAdapter::class.java.simpleName, "picasso image loading issue: ${e.message}", e)
                    }
                }
            }
        }
    }

}