package com.android4you.contactsprovider.data.repository

import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class FakeContactRepository(
    initial: List<Contact> = defaultContacts
) : ContactRepository {

    private val contacts = MutableStateFlow(initial)

    /** When set, [observeContacts] fails with this error as soon as it is collected. */
    var error: Throwable? = null

    fun emit(newContacts: List<Contact>) {
        contacts.value = newContacts
    }

    override suspend fun getContacts(): List<Contact> = contacts.value

    override fun observeContacts(): Flow<List<Contact>> = flow {
        error?.let { throw it }
        emitAll(contacts)
    }

    companion object {
        val defaultContacts = listOf(
            Contact("1", "John Doe", listOf("1234567890")),
            Contact("2", "Jane Smith", emptyList())
        )
    }
}
