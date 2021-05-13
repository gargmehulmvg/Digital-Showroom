package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.VariantItemResponse

interface IVariantItemClickListener {

    fun onVariantEditNameClicked(variant: VariantItemResponse?, position: Int)

    fun onVariantDeleteClicked(position: Int)

}