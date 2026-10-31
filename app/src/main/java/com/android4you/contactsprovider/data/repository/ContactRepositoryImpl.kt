package com.android4you.contactsprovider.data.repository

import android.content.ContentResolver
import android.database.ContentObserver
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.android4you.contactsprovider.di.IoDispatcher
import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.repository.ContactRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContactRepository {

    override suspend fun getContacts(): List<Contact> = withContext(ioDispatcher) {
        // Phone.CONTENT_URI returns one row per phone number, so rows are grouped by contact id.
        val contacts = LinkedHashMap<String, Contact>()

        contentResolver.query(
            Phone.CONTENT_URI,
            PROJECTION,
            null,
            null,
            "${Phone.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(Phone.CONTACT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndexOrThrow(Phone.NUMBER)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idIndex) ?: continue
                val number = cursor.getString(numberIndex)?.trim()?.takeIf { it.isNotEmpty() }

                val existing = contacts[id]
                contacts[id] = when {
                    existing == null -> Contact(
                        id = id,
                        name = cursor.getString(nameIndex).orEmpty(),
                        phoneNumbers = listOfNotNull(number)
                    )
                    number != null && number !in existing.phoneNumbers ->
                        existing.copy(phoneNumbers = existing.phoneNumbers + number)
                    else -> existing
                }
            }
        }

        contacts.values.toList()
    }

    override fun observeContacts(): Flow<List<Contact>> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            observer
        )
        trySend(Unit) // initial load
        awaitClose { contentResolver.unregisterContentObserver(observer) }
    }
        .conflate() // bursts of change notifications collapse into a single reload
        .map { getContacts() }

    private companion object {
        val PROJECTION = arrayOf(Phone.CONTACT_ID, Phone.DISPLAY_NAME, Phone.NUMBER)
    }
}
