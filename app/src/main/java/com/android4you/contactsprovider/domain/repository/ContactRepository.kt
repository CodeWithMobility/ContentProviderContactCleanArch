package com.android4you.contactsprovider.domain.repository

import com.android4you.contactsprovider.domain.model.Contact

interface ContactRepository {
    suspend fun getContacts(): List<Contact>
}
