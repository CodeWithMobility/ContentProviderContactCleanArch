package com.android4you.contactsprovider.data.repository

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun Cursor.getSafeString(index: Int): String? =
    runCatching { getString(index) }.getOrNull()

class ContactRepositoryImpl(
    private val contentResolver: ContentResolver
) : ContactRepository {

    override suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            if (idIndex == -1 || nameIndex == -1 || numberIndex == -1) {
                return@withContext contacts
            }

            while (it.moveToNext()) {
                val id = it.getSafeString(idIndex) ?: continue
                val name = it.getSafeString(nameIndex)
                val number = it.getSafeString(numberIndex)
                contacts.add(Contact(id, name, number))
            }
        }

        contacts
    }
}
