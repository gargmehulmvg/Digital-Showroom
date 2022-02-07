package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.LeadsResponse

interface ILeadsListItemListener {

    fun onLeadsItemCLickChanged(item: LeadsResponse?)

}