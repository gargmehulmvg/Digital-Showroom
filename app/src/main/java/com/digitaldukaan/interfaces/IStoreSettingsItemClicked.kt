package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.StoreOptionsResponse
import com.digitaldukaan.models.response.TrendingListResponse

interface IStoreSettingsItemClicked {

    fun onStoreSettingItemClicked(subPagesResponse: StoreOptionsResponse)

    fun onNewReleaseItemClicked(responseItem: TrendingListResponse?)
}