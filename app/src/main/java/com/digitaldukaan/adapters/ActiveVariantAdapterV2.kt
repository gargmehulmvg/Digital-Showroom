package com.digitaldukaan.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.isDouble
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.IVariantItemClickListener
import com.digitaldukaan.models.response.AddProductStaticText
import com.digitaldukaan.models.response.VariantItemResponse
import com.google.android.material.textfield.TextInputLayout

class ActiveVariantAdapterV2(
    private var mContext:           Context?,
    private var mStaticText:        AddProductStaticText?,
    private var mActiveVariantList: ArrayList<VariantItemResponse>?,
    private var mListener:          IVariantItemClickListener?
) :
    RecyclerView.Adapter<ActiveVariantAdapterV2.ActiveVariantViewHolder>() {

    companion object {
        private const val TAG = "ActiveVariantAdapterV2"
    }

    inner class ActiveVariantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val imageViewContainer: View = itemView.findViewById(R.id.imageViewContainer)
        val noImagesLayout: View = itemView.findViewById(R.id.noImagesLayout)
        val priceEditText: EditText = itemView.findViewById(R.id.priceEditText)
        val discountPriceEditText: EditText = itemView.findViewById(R.id.discountPriceEditText)
        val variantNameInputLayout: TextInputLayout = itemView.findViewById(R.id.variantNameInputLayout)
        val variantPriceInputLayout: TextInputLayout = itemView.findViewById(R.id.variantPriceInputLayout)
        val deleteTextView: TextView = itemView.findViewById(R.id.deleteTextView)
        val variantDiscountPriceInputLayout: TextInputLayout = itemView.findViewById(R.id.variantDiscountPriceInputLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveVariantViewHolder {
        val view = ActiveVariantViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_active_variant_item_v2, parent, false)
        )
        view.deleteTextView.setOnClickListener { mListener?.onVariantDeleteClicked(view.adapterPosition) }
        view.imageViewContainer.setOnClickListener { mListener?.onVariantImageClicked(view.adapterPosition) }
        return view
    }

    override fun getItemCount(): Int = mActiveVariantList?.size ?: 0

    override fun onBindViewHolder(
        holder: ActiveVariantViewHolder,
        position: Int
    ) {
        val item = mActiveVariantList?.get(position)
        holder.apply {
            deleteTextView.text = mStaticText?.text_delete
            variantNameInputLayout.hint = mStaticText?.hint_variant_name
            variantPriceInputLayout.hint = mStaticText?.hint_price
            variantDiscountPriceInputLayout.hint = mStaticText?.hint_discounted_price
            if (0.0 != item?.price) priceEditText.setText("${item?.price}")
            if (0.0 != item?.discountedPrice) discountPriceEditText.setText("${item?.discountedPrice}")
            discountPriceEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "beforeTextChanged: ")
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "onTextChanged: ")
                }

                override fun afterTextChanged(editable: Editable?) {
                    val str = editable?.toString()
                    if (!isEmpty(str)) {
                        val priceStr = priceEditText.text.toString()
                        when {
                            isEmpty(priceStr) -> {
                                if (str != "0.0") {
                                    priceEditText.apply {
                                        error = mStaticText?.error_mandatory_field
                                        requestFocus()
                                    }
                                    discountPriceEditText.text = null
                                }
                                return
                            }
                            "." == str -> {}
                            !isDouble(str) -> {
                                discountPriceEditText.apply {
                                    text = null
                                    error = mStaticText?.error_mandatory_field
                                    requestFocus()
                                }
                            }
                            priceStr.toDouble() < str?.toDouble() ?: 0.0 -> {
                                discountPriceEditText.apply {
                                    error = mStaticText?.error_discount_price_less_then_original_price
                                    text = null
                                    requestFocus()
                                }
                            }
                        }
                    }
                }

            })
            if (isEmpty(item?.variantImagesList)) {
                noImagesLayout.visibility = View.VISIBLE
                imageView.visibility = View.GONE
            } else {
                imageView.visibility = View.VISIBLE
                noImagesLayout.visibility = View.GONE
                mContext?.let { context -> Glide.with(context).load(item?.variantImagesList?.get(0)?.imageUrl).into(imageView) }
            }
        }
    }

    fun deleteItemFromActiveVariantList(position: Int) {
        mActiveVariantList?.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mActiveVariantList?.size ?: 0)
        if (isEmpty(mActiveVariantList)) mListener?.onVariantListEmpty()
    }

}