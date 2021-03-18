package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.OrderItemResponse

interface IOrderListItemListener {

    fun onOrderCheckBoxChanged(isChecked: Boolean, item: OrderItemResponse?)

    fun onOrderItemCLickChanged(item: OrderItemResponse?)

}