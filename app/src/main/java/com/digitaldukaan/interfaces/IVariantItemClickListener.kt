package com.digitaldukaan.interfaces

interface IVariantItemClickListener {

    fun onVariantDeleteClicked(position: Int)

    fun onVariantImageClicked(position: Int)

    fun onVariantListEmpty()

    fun onVariantItemChanged()

    fun onVariantNameChangedListener(variantUpdatedName: String?, variantPosition: Int)

    fun onVariantInventoryIconClicked(position: Int)

}