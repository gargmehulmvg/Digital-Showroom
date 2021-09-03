package com.digitaldukaan.interfaces

interface IVariantItemClickListener {

    fun onVariantDeleteClicked(position: Int)

    fun onVariantImageClicked(position: Int)

    fun onVariantListEmpty()

    fun onVariantItemChanged()

}