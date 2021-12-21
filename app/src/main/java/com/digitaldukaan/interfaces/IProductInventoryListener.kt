package com.digitaldukaan.interfaces

import com.digitaldukaan.models.dto.InventoryEnableDTO

interface IProductInventoryListener {

    fun onItemInventoryChangeListener(inventoryCount: String)

    fun onVariantInventoryChangeListener(inventoryCount: String, position: Int)

    fun onVariantInventoryItemCheckChange(isCheck: Boolean, inventoryItem: InventoryEnableDTO, position: Int)

}