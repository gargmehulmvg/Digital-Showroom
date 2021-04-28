package com.digitaldukaan.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.models.response.AddProductStaticText
import com.digitaldukaan.models.response.MasterCatalogItemResponse
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso

class MasterCatalogItemsConfirmationAdapter(
    private val mActivity: MainActivity,
    private var mStaticText: AddProductStaticText? = null,
    private val addMasterCatalogConfirmProductsList: ArrayList<MasterCatalogItemResponse?>
) :
    RecyclerView.Adapter<MasterCatalogItemsConfirmationAdapter.MarketingCardViewHolder>() {

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val priceLayout: TextInputLayout = itemView.findViewById(R.id.priceLayout)
        val priceEditText: EditText = itemView.findViewById(R.id.priceEditText)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val container: View = itemView.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        val view =  MarketingCardViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.master_catalog_confirmation_item, parent, false)
    )
        view.priceEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                val str = editable.toString()
                addMasterCatalogConfirmProductsList[view.adapterPosition]?.price = if (str.isNotEmpty()) str.toDouble() else 0.0
            }
        })
        return view
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int = addMasterCatalogConfirmProductsList.size

    override fun onBindViewHolder(
        holder: MarketingCardViewHolder,
        position: Int
    ) {
        val item = addMasterCatalogConfirmProductsList[position]
        holder.run {
            Picasso.get().load(item?.imageUrl).into(imageView)
            titleTextView.text = item?.itemName
            priceLayout.hint = "${mStaticText?.hint_mrp} (${mStaticText?.text_rupees_symbol})"
            titleTextView.setTextColor(ContextCompat.getColor(mActivity, if (item?.isSelected == true) R.color.open_green else R.color.black))
            if (item?.isAdded == true) {
                container.isEnabled = false
                container.alpha = 0.2f
            }
            priceEditText.setText("${item?.price}")
        }
    }

}