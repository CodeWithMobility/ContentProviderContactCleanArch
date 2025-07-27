package com.android4you.contactsprovider.domain.repository

import com.android4you.contactsprovider.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>

}
