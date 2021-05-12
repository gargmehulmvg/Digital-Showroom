package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.VariantItemResponse

interface IVariantItemClickListener {

    fun onVariantItemClickListener(position: Int)

    fun onVariantEditNameClicked(variant: VariantItemResponse?, position: Int)

    fun onVariantDeleteClicked(position: Int)

}