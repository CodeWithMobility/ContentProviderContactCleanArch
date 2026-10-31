package com.android4you.contactsprovider.domain.model

/**
 * A device contact. A contact may own several phone numbers, so they are grouped here
 * instead of producing one [Contact] per number.
 */
data class Contact(
    val id: String,
    val name: String,
    val phoneNumbers: List<String> = emptyList()
)
