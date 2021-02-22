package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.SubPagesResponse

interface IAppSettingsItemClicked {

    fun onAppSettingItemClicked(subPagesResponse: SubPagesResponse)
}