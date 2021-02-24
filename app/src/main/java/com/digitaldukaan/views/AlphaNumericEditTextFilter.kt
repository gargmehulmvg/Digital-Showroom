package com.digitaldukaan.views

import android.text.InputFilter
import android.widget.EditText


fun EditText.allowOnlyAlphaNumericCharacters() {
    filters = filters.plus(
        listOf(
            InputFilter { s, _, _, _, _, _->
                s.replace(Regex("[^A-Za-z0-9]"), "")
            },
            InputFilter.AllCaps()
        )
    )
}