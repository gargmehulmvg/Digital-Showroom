package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.digitaldukaan.R
import com.digitaldukaan.models.dto.ImageFolder

class MultiImageSelectionAdapter(private var mImagesList: ArrayList<ImageFolder>, private var mContext: Context?) :
    RecyclerView.Adapter<MultiImageSelectionAdapter.MultiImagePickerViewHolder>() {

    inner class MultiImagePickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderPicImageView: ImageView = itemView.findViewById(R.id.folderPic)
        val folderSizeTextView: TextView = itemView.findViewById(R.id.folderSize)
        val folderNameTextView: TextView = itemView.findViewById(R.id.folderName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiImagePickerViewHolder {
        val view = MultiImagePickerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.multi_image_selection_item, parent, false))
        view.folderPicImageView.setOnClickListener {
            val position = view.absoluteAdapterPosition
            if (position < 0 || position > mImagesList.size) return@setOnClickListener
        }
        return view
    }

    override fun getItemCount(): Int = mImagesList.size

    override fun onBindViewHolder(holder: MultiImagePickerViewHolder, position: Int) {
        val folder = mImagesList[position]
        mContext?.let { context -> Glide.with(context)
            .load(folder.firstPic)
            .apply(RequestOptions().centerCrop())
            .into(holder.folderPicImageView)
        }
        val text = "" + folder.folderName
        val folderSizeString = "" + folder.numberOfPics.toString() + " Media"
        holder.folderSizeTextView.text = folderSizeString
        holder.folderNameTextView.text = text
    }

}