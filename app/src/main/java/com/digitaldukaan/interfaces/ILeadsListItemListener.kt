package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.LeadsItemResponse

interface ILeadsListItemListener {

    fun onLeadsItemCLickChanged(item: LeadsItemResponse?)

}