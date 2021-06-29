package com.digitaldukaan.interfaces

import com.digitaldukaan.models.dto.ContactModel

interface IContactItemClicked {

    fun onContactItemClicked(contact: ContactModel)
}