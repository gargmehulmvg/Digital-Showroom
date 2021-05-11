package com.digitaldukaan.smsapi

interface ISmsReceivedListener {

    fun onNewSmsReceived(sms: String?)
}