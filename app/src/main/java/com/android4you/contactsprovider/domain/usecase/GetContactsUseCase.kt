package com.android4you.contactsprovider.domain.usecase

import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.repository.ContactRepository

class GetContactsUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(): List<Contact> = repository.getContacts()
}
