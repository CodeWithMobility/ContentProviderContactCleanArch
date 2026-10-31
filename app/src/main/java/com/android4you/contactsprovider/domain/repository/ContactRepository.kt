package com.android4you.contactsprovider.domain.repository

import com.android4you.contactsprovider.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {

    /** One-shot read of all contacts that have at least one phone row. */
    suspend fun getContacts(): List<Contact>

    /**
     * Emits the current contacts immediately and again every time the device's contacts change.
     * Throws [SecurityException] when READ_CONTACTS has not been granted.
     */
    fun observeContacts(): Flow<List<Contact>>
}
