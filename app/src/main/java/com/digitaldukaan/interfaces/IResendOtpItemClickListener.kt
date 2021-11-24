package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.*

interface IResendOtpItemClickListener {

    fun onStaticDataResponse(staticDataResponse: CommonApiResponse)

    fun onResendOtpItemClickListener(position: Int, item: CommonCtaResponse?)

}