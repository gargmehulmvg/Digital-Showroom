package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.LeadsFilterOptionsItemResponse

interface ILeadsFilterItemClickListener {

    fun onLeadsFilterItemClickListener(item: LeadsFilterOptionsItemResponse?, filterType:String?)

}