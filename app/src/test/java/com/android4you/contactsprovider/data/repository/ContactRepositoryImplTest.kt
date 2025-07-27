package com.android4you.contactsprovider.data.repository

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import com.android4you.contactsprovider.domain.repository.ContactRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ContactRepositoryImplTest {

    private lateinit var contentResolver: ContentResolver
    private lateinit var repository: ContactRepository

    @Before
    fun setUp() {
        contentResolver = mockk()
        repository = ContactRepositoryImpl(contentResolver)
    }

    @Test
    fun `getContacts returns one contact from content resolver`() = runTest {
        val cursor = mockk<Cursor>()

        every { contentResolver.query(any(), any(), any(), any(), any()) } returns cursor
        every { cursor.moveToNext() } returns true andThen false
        every { cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID) } returns 0
        every { cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) } returns 1
        every { cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) } returns 2

        every { cursor.getString(0) } returns "1"
        every { cursor.getString(1) } returns "John Doe"
        every { cursor.getString(2) } returns "1234567890"

        every { cursor.close() } just Runs

        val result = repository.getContacts().first()

        assertEquals(1, result.size)
        assertEquals("John Doe", result[0].name)
        assertEquals("1234567890", result[0].phoneNumber)
    }

    @Test
    fun `getContacts returns multiple contacts`() = runTest {
        val cursor = mockk<Cursor>(relaxed = true)
        var row = 0

        every { contentResolver.query(any(), any(), any(), any(), any()) } returns cursor
        every { cursor.moveToNext() } answers { row++ < 2 }
        every { cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID) } returns 0
        every { cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) } returns 1
        every { cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) } returns 2

        every { cursor.getString(0) } answers { if (row == 1) "1" else "2" }
        every { cursor.getString(1) } answers { if (row == 1) "John" else "Alice" }
        every { cursor.getString(2) } answers { if (row == 1) "111111" else "222222" }
        every { cursor.close() } just Runs

        val contacts = repository.getContacts().first()

        assertEquals(2, contacts.size)
        assertEquals("John", contacts[0].name)
        assertEquals("Alice", contacts[1].name)
    }

    @Test
    fun `getContacts returns empty list when cursor is null`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        val contacts = repository.getContacts().first()

        assertTrue(contacts.isEmpty())
    }

    @Test
    fun `getContacts handles null phone number`() = runTest {
        val cursor = mockk<Cursor>()
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns cursor
        every { cursor.moveToNext() } returns true andThen false
        every { cursor.getColumnIndex(any()) } returnsMany listOf(0, 1, 2)

        every { cursor.getString(0) } returns "1"
        every { cursor.getString(1) } returns "John"
        every { cursor.getString(2) } returns null

        every { cursor.close() } just Runs

        val contacts = repository.getContacts().first()

        assertEquals(1, contacts.size)
        assertEquals("", contacts[0].phoneNumber)
    }

    @Test
    fun `getContacts handles null name`() = runTest {
        val cursor = mockk<Cursor>()
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns cursor
        every { cursor.moveToNext() } returns true andThen false
        every { cursor.getColumnIndex(any()) } returnsMany listOf(0, 1, 2)

        every { cursor.getString(0) } returns "1"
        every { cursor.getString(1) } returns null
        every { cursor.getString(2) } returns "123456"

        every { cursor.close() } just Runs

        val contacts = repository.getContacts().first()

        assertEquals(1, contacts.size)
        assertEquals("", contacts[0].name)
    }

    @Test
    fun `getContacts handles empty values`() = runTest {
        val cursor = mockk<Cursor>()
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns cursor
        every { cursor.moveToNext() } returns true andThen false
        every { cursor.getColumnIndex(any()) } returnsMany listOf(0, 1, 2)

        every { cursor.getString(0) } returns ""
        every { cursor.getString(1) } returns ""
        every { cursor.getString(2) } returns ""

        every { cursor.close() } just Runs

        val contacts = repository.getContacts().first()

        assertEquals(1, contacts.size)
        assertEquals("", contacts[0].name)
        assertEquals("", contacts[0].phoneNumber)
    }
}


