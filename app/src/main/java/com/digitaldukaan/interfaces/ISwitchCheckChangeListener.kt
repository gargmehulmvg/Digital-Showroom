package com.digitaldukaan.interfaces

import android.widget.CompoundButton

interface ISwitchCheckChangeListener {

    fun onSwitchCheckChangeListener(switch: CompoundButton?, isChecked: Boolean, paymentType: String?)

}