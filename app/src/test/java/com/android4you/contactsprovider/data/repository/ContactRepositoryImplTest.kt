package com.android4you.contactsprovider.data.repository

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract.CommonDataKinds.Phone
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** One row of the Phone table: (contact id, display name, number). */
private data class Row(val id: String?, val name: String?, val number: String?)

@OptIn(ExperimentalCoroutinesApi::class)
class ContactRepositoryImplTest {

    private lateinit var contentResolver: ContentResolver
    private lateinit var repository: ContactRepositoryImpl

    @Before
    fun setUp() {
        contentResolver = mockk()
        repository = ContactRepositoryImpl(contentResolver, UnconfinedTestDispatcher())
    }

    private fun givenRows(vararg rows: Row) {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns cursorOf(*rows)
    }

    private fun cursorOf(vararg rows: Row): Cursor {
        val cursor = mockk<Cursor>(relaxed = true)
        var position = -1
        every { cursor.getColumnIndexOrThrow(Phone.CONTACT_ID) } returns 0
        every { cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME) } returns 1
        every { cursor.getColumnIndexOrThrow(Phone.NUMBER) } returns 2
        every { cursor.moveToNext() } answers { ++position < rows.size }
        every { cursor.getString(0) } answers { rows[position].id }
        every { cursor.getString(1) } answers { rows[position].name }
        every { cursor.getString(2) } answers { rows[position].number }
        return cursor
    }

    @Test
    fun `maps a single row to a contact`() = runTest {
        givenRows(Row("1", "John Doe", "1234567890"))

        val result = repository.getContacts()

        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
        assertEquals("John Doe", result[0].name)
        assertEquals(listOf("1234567890"), result[0].phoneNumbers)
    }

    @Test
    fun `groups several numbers of the same contact`() = runTest {
        givenRows(
            Row("1", "John", "111"),
            Row("1", "John", "222"),
            Row("2", "Alice", "333")
        )

        val result = repository.getContacts()

        assertEquals(listOf("John", "Alice"), result.map { it.name })
        assertEquals(listOf("111", "222"), result[0].phoneNumbers)
        assertEquals(listOf("333"), result[1].phoneNumbers)
    }

    @Test
    fun `removes duplicate numbers of the same contact`() = runTest {
        givenRows(
            Row("1", "John", "111"),
            Row("1", "John", "111")
        )

        val result = repository.getContacts()

        assertEquals(1, result.size)
        assertEquals(listOf("111"), result[0].phoneNumbers)
    }

    @Test
    fun `returns empty list when cursor is null`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        assertTrue(repository.getContacts().isEmpty())
    }

    @Test
    fun `null name becomes empty string`() = runTest {
        givenRows(Row("1", null, "123456"))

        val result = repository.getContacts()

        assertEquals("", result[0].name)
        assertEquals(listOf("123456"), result[0].phoneNumbers)
    }

    @Test
    fun `null or blank numbers are skipped`() = runTest {
        givenRows(
            Row("1", "John", null),
            Row("2", "Jane", "   ")
        )

        val result = repository.getContacts()

        assertEquals(2, result.size)
        assertTrue(result.all { it.phoneNumbers.isEmpty() })
    }

    @Test
    fun `rows without a contact id are ignored`() = runTest {
        givenRows(
            Row(null, "Ghost", "999"),
            Row("1", "John", "111")
        )

        val result = repository.getContacts()

        assertEquals(listOf("John"), result.map { it.name })
    }

    @Test
    fun `propagates SecurityException when permission is missing`() = runTest {
        every {
            contentResolver.query(any(), any(), any(), any(), any())
        } throws SecurityException("READ_CONTACTS")

        val failure = runCatching { repository.getContacts() }.exceptionOrNull()

        assertTrue(failure is SecurityException)
    }
}
