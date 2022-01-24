package com.digitaldukaan.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import kotlin.math.roundToInt

class MultiImageSelectionAdapter(private var mImagesList: ArrayList<String>) :
    RecyclerView.Adapter<MultiImageSelectionAdapter.MultiImagePickerViewHolder>() {

    companion object {
        private const val HEIGHT    = 300
        private const val WIDTH     = 300
    }

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
        holder.imageView.setImageBitmap(getScaledBitmap(imageStr))
    }

    private fun getScaledBitmap(picturePath: String): Bitmap? {
        val sizeOptions = BitmapFactory.Options()
        sizeOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(picturePath, sizeOptions)
        val inSampleSize = calculateInSampleSize(sizeOptions)
        sizeOptions.inJustDecodeBounds = false
        sizeOptions.inSampleSize = inSampleSize
        return BitmapFactory.decodeFile(picturePath, sizeOptions)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > HEIGHT || width > WIDTH) {
            val heightRatio = (height.toFloat() / HEIGHT).roundToInt()
            val widthRatio = (width.toFloat() / WIDTH).roundToInt()
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

}