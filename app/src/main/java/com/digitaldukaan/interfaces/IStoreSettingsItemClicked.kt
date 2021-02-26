package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.StoreOptionsResponse

interface IStoreSettingsItemClicked {

    fun onStoreSettingItemClicked(subPagesResponse: StoreOptionsResponse)
}