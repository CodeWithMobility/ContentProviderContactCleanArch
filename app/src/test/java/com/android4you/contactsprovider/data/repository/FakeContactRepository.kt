package com.android4you.contactsprovider.data.repository

import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.repository.ContactRepository

class FakeContactRepository : ContactRepository {
    override suspend fun getContacts(): List<Contact> {
        return listOf(
            Contact("1", "John Doe", "1234567890"),
            Contact("2", "Jane Smith", null)
        )
    }
}
