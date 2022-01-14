package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.MoreControlsInnerItemResponse

interface IMoreControlsItemClickListener {

    fun onMoreControlsEditMinOrderValueClicked(item: MoreControlsInnerItemResponse)

    fun onMoreControlsSetDeliveryChargeClicked(item: MoreControlsInnerItemResponse)

    fun onMoreControlsEditCustomerAddressFieldClicked(item: MoreControlsInnerItemResponse)

    fun onMoreControlsPrepaidOrderClicked(item: MoreControlsInnerItemResponse)

    fun onMoreControlsPaymentModesClicked(item: MoreControlsInnerItemResponse)

    fun onMoreControlsOrderNotificationClicked(item: MoreControlsInnerItemResponse)
}