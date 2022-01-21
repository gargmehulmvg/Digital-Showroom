package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.OfferInfoArray

interface IActiveOfferDetailsListener {

    fun onActiveOfferDetailsListener(offerInfoMap: ArrayList<OfferInfoArray>?)

}