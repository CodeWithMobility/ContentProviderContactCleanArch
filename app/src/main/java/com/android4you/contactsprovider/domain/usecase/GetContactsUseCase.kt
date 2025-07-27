package com.android4you.contactsprovider.domain.usecase

import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow

class GetContactsUseCase(private val repository: ContactRepository) {
    operator fun invoke(): Flow<List<Contact>> = repository.getContacts()
}
