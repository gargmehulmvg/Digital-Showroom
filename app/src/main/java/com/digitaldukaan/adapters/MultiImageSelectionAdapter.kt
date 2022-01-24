package com.digitaldukaan.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R

class MultiImageSelectionAdapter(
    private var mImagesList: ArrayList<String>,
    private var mContext: Context?) :
    RecyclerView.Adapter<MultiImageSelectionAdapter.MultiImagePickerViewHolder>() {

    inner class MultiImagePickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.container)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiImagePickerViewHolder {
        val view = MultiImagePickerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.multi_image_selection_item, parent, false))
        view.container.setOnClickListener {
            val position = view.absoluteAdapterPosition
            if (position < 0 || position > mImagesList.size) return@setOnClickListener
        }
        return view
    }


    override fun getItemCount(): Int = mImagesList.size

    override fun onBindViewHolder(holder: MultiImagePickerViewHolder, position: Int) {
        val imageStr = mImagesList[position]
        holder.imageView.setImageBitmap(BitmapFactory.decodeFile(imageStr))
    }

}