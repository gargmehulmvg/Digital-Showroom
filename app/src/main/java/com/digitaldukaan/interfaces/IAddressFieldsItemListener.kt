package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.AddressFieldsItemResponse

interface IAddressFieldsItemListener {

    fun onAddressFieldsItemNotifyListener(position: Int)

    fun onAddressFieldsCheckChangeListener(item: AddressFieldsItemResponse?)

    fun onAddressFieldsMandatorySwitchChangeListener(item: AddressFieldsItemResponse?)

}