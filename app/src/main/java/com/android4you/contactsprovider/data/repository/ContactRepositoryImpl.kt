package com.android4you.contactsprovider.data.repository

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

fun Cursor.getSafeString(index: Int): String = getString(index) ?: ""


class ContactRepositoryImpl(
    private val contentResolver: ContentResolver
) : ContactRepository {

    override fun getContacts(): Flow<List<Contact>> = flow {
        emit(fetchContactsOnce())
    }

     suspend fun fetchContactsOnce(): List<Contact> = withContext(Dispatchers.IO) {
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

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex)?: ""
                val number = it.getString(numberIndex)?:""
                contacts.add(Contact(id, name, number))
            }
        }
        contacts
    }
}
