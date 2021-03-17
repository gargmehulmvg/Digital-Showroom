package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.OrderItemResponse

interface IOrderCheckBoxListener {

    fun onOrderCheckBoxChanged(isChecked: Boolean, item: OrderItemResponse?)

}