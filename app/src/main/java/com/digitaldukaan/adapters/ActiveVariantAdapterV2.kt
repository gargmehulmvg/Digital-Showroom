package com.digitaldukaan.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.isDouble
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IVariantItemClickListener
import com.digitaldukaan.models.response.AddProductStaticText
import com.digitaldukaan.models.response.VariantItemResponse
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout

class ActiveVariantAdapterV2(
    private var mContext:           Context?,
    private var mStaticText:        AddProductStaticText?,
    private var mRecentVariantsList: ArrayList<VariantItemResponse>?,
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
        val nameEditText: AppCompatAutoCompleteTextView = itemView.findViewById(R.id.nameEditText)
        val discountPriceEditText: EditText = itemView.findViewById(R.id.discountPriceEditText)
        val variantNameInputLayout: TextInputLayout = itemView.findViewById(R.id.variantNameInputLayout)
        val variantPriceInputLayout: TextInputLayout = itemView.findViewById(R.id.variantPriceInputLayout)
        val deleteTextView: TextView = itemView.findViewById(R.id.deleteTextView)
        val inStockTextView: TextView = itemView.findViewById(R.id.inStockTextView)
        val variantSwitch: SwitchMaterial = itemView.findViewById(R.id.variantSwitch)
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
            if (1 == item?.available) {
                variantSwitch.isChecked = true
                inStockTextView.text = mStaticText?.text_in_stock
            } else {
                variantSwitch.isChecked = false
                inStockTextView.text = mContext?.getString(R.string.out_of_stock)
            }
            deleteTextView.text = mStaticText?.text_delete
            variantNameInputLayout.hint = mStaticText?.hint_variant_name
            variantPriceInputLayout.hint = mStaticText?.hint_price
            variantDiscountPriceInputLayout.hint = mStaticText?.hint_discounted_price
            if (isNotEmpty(item?.variantName)) nameEditText.setText(item?.variantName) else nameEditText.text = null
            if (0.0 != item?.price) priceEditText.setText("${item?.price}") else if (0.0 == item.price && 0.0 == mActiveVariantList?.get(0)?.price) { priceEditText.text = null } else priceEditText.setText("${mActiveVariantList?.get(0)?.price}")
            if (0.0 != item?.discountedPrice) discountPriceEditText.setText("${item?.discountedPrice}") else if (0.0 == item.discountedPrice && 0.0 == mActiveVariantList?.get(0)?.discountedPrice) { discountPriceEditText.text = null } else discountPriceEditText.setText("${mActiveVariantList?.get(0)?.discountedPrice}")
            variantNameInputLayout.error = if (item?.isVariantNameEmptyError == true) mContext?.getString(R.string.mandatory_field_message) else null
            priceEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "beforeTextChanged: ")
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "onTextChanged: ")
                }

                override fun afterTextChanged(editable: Editable?) {
                    val str = editable?.toString()
                    variantPriceInputLayout.error = null
                    variantDiscountPriceInputLayout.error = null
                    if (isNotEmpty(str)) {
                        val discountPriceStr = discountPriceEditText.text.toString()
                        when {
                            "." == str -> {}
                            !isDouble(str) -> {
                                discountPriceEditText.apply {
                                    text = null
                                    requestFocus()
                                }
                                variantDiscountPriceInputLayout.error = mStaticText?.error_mandatory_field
                            }
                            (if (isEmpty(discountPriceStr)) 0.0 else discountPriceStr.toDouble()) > str?.toDouble() ?: 0.0 -> {
                                discountPriceEditText.apply {
                                    text = null
                                }
                                item?.discountedPrice = 0.0
                            }
                            else -> item?.price = str?.toDouble() ?: 0.0
                        }
                    }
                    mListener?.onVariantItemChanged()
                }

            })
            nameEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "beforeTextChanged: ")
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "onTextChanged: ")
                }

                override fun afterTextChanged(editable: Editable?) {
                    val str = editable?.toString()?.trim()
                    item?.variantName = str
                    if (isNotEmpty(str)) {
                        variantNameInputLayout.error = null
                        item?.isVariantNameEmptyError = false
                    }
                    mListener?.onVariantItemChanged()
                }

            })
            discountPriceEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "beforeTextChanged: ")
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "onTextChanged: ")
                }

                override fun afterTextChanged(editable: Editable?) {
                    val str = editable?.toString()
                    variantPriceInputLayout.error = null
                    variantDiscountPriceInputLayout.error = null
                    if (!isEmpty(str)) {
                        val priceStr = priceEditText.text.toString()
                        when {
                            isEmpty(priceStr) -> {
                                if ("0.0" != str) {
                                    variantPriceInputLayout.error = mStaticText?.error_mandatory_field
                                    priceEditText.apply {
                                        requestFocus()
                                    }
                                    discountPriceEditText.text = null
                                }
                                return
                            }
                            "." == str -> {}
                            !isDouble(str) -> {
                                variantDiscountPriceInputLayout.error = mStaticText?.error_mandatory_field
                                discountPriceEditText.apply {
                                    text = null
                                    requestFocus()
                                }
                            }
                            priceStr.toDouble() < str?.toDouble() ?: 0.0 -> {
                                discountPriceEditText.apply {
                                    text = null
                                    requestFocus()
                                }
                                variantDiscountPriceInputLayout.error = mStaticText?.error_discount_price_less_then_original_price
                            }
                            else -> item?.discountedPrice = str?.toDouble() ?: 0.0
                        }
                    }
                    mListener?.onVariantItemChanged()
                }

            })
            if (isEmpty(item?.variantImagesList)) {
                noImagesLayout.visibility = View.VISIBLE
                imageView.visibility = View.GONE
            } else {
                val imageItem = item?.variantImagesList?.get(0)
                if (0 == imageItem?.status || isEmpty(imageItem?.imageUrl ?: "")) {
                    noImagesLayout.visibility = View.VISIBLE
                    imageView.visibility = View.GONE
                } else {
                    imageView.visibility = View.VISIBLE
                    noImagesLayout.visibility = View.GONE
                    mContext?.let { context ->
                        Glide.with(context).load(imageItem?.imageUrl).into(imageView)
                    }
                }
            }
            variantSwitch.setOnCheckedChangeListener { _, isChecked ->
                mListener?.onVariantItemChanged()
                if (isChecked) {
                    item?.available = 1
                    inStockTextView.text = mStaticText?.text_in_stock
                } else {
                    item?.available = 0
                    inStockTextView.text = mContext?.getString(R.string.out_of_stock)
                }
            }
            mContext?.let { context ->
                val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, getRecentVariantList())
                nameEditText.setAdapter(adapter)
                nameEditText.threshold = 1
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun deleteItemFromActiveVariantList(position: Int) {
        mActiveVariantList?.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mActiveVariantList?.size ?: 0)
        if (isEmpty(mActiveVariantList)) mListener?.onVariantListEmpty()
        mListener?.onVariantItemChanged()
    }

    private val mRecentVariantNameListStr = ArrayList<String>()

    private fun getRecentVariantList(): ArrayList<String> {
        if (isNotEmpty(mRecentVariantNameListStr)) return mRecentVariantNameListStr
        mRecentVariantsList?.forEachIndexed { _, itemResponse ->
            mRecentVariantNameListStr.add(itemResponse.variantName ?: "")
        }
        return mRecentVariantNameListStr
    }

}