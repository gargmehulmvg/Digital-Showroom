package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.VariantItemResponse

interface IVariantItemClickListener {

    fun onVariantItemClickListener(position: Int)

    fun onVariantEditNameClicked(variant: VariantItemResponse?)

    fun onVariantDeleteClicked(position: Int)

}