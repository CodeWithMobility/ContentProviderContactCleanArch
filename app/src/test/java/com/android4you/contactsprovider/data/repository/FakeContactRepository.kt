package com.android4you.contactsprovider.data.repository

import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeContactRepository(
    private val shouldFail: Boolean = false,
    private val contacts: List<Contact> = emptyList()
) : ContactRepository {
    override fun getContacts(): Flow<List<Contact>> = flow {
        if (shouldFail) {
            throw RuntimeException("Repository failure")
        } else {
            emit(contacts)
        }
    }
}
