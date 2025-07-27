package com.android4you.contactsprovider.domain.model

data class Contact(
    val id: String,
    val name: String, // Make nullable
    val phoneNumber: String?
)
