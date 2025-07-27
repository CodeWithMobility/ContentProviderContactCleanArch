package com.android4you.contactsprovider.data.repository

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.test.core.app.ApplicationProvider
import com.android4you.contactsprovider.domain.model.Contact
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ContactRepositoryRoboElectricTest {

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var repository: ContactRepositoryImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        contentResolver = context.contentResolver
        repository = ContactRepositoryImpl(contentResolver)
    }

    @Test
    fun `getContacts returns expected contacts`() = runTest {
        // Given
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val columns = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = MatrixCursor(columns).apply {
            addRow(arrayOf("1", "Alice", "1234567890"))
            addRow(arrayOf("2", "Bob", "9876543210"))
        }

        // Register the fake cursor with ShadowContentResolver
        ShadowContentResolver.registerProviderInternal(
            uri.authority!!,
            object : ContentProvider() {
                override fun onCreate(): Boolean = true
                override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?
                ): Cursor {
                    return cursor
                }

                override fun getType(uri: Uri) = null
                override fun insert(uri: Uri, values: ContentValues?) = null
                override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
                override fun update(
                    uri: Uri,
                    values: ContentValues?,
                    selection: String?,
                    selectionArgs: Array<out String>?
                ) = 0
            }
        )

        // When
        val result = repository.getContacts().first()

        // Then
        val expected = listOf(
            Contact("1", "Alice", "1234567890"),
            Contact("2", "Bob", "9876543210")
        )

        assertEquals(expected, result)
    }

    @Test
    fun `getContacts returns empty list when cursor is empty`() = runTest {
        // Arrange
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val columns = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val emptyCursor = MatrixCursor(columns) // no rows

        ShadowContentResolver.registerProviderInternal(
            uri.authority!!,
            object : ContentProvider() {
                override fun onCreate() = true
                override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?
                ) = emptyCursor

                override fun getType(uri: Uri) = null
                override fun insert(uri: Uri, values: ContentValues?) = null
                override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
                override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
            }
        )

        // Act
        val result = repository.getContacts().first()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getContacts handles null display name and number gracefully`() = runTest {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val columns = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = MatrixCursor(columns).apply {
            addRow(arrayOf("1", null, "9876543210"))
            addRow(arrayOf("2", "Charlie", null))
        }

        ShadowContentResolver.registerProviderInternal(
            uri.authority!!,
            object : ContentProvider() {
                override fun onCreate() = true
                override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?
                ) = cursor

                override fun getType(uri: Uri) = null
                override fun insert(uri: Uri, values: ContentValues?) = null
                override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
                override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
            }
        )

        val result = repository.getContacts().first()

        val expected = listOf(
            Contact("1", "", "9876543210"),
            Contact("2", "Charlie", "")
        )

        assertEquals(expected, result)
    }

    @Test
    fun `getContacts returns all rows even with duplicate contact IDs`() = runTest {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val columns = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = MatrixCursor(columns).apply {
            addRow(arrayOf("1", "David", "11111"))
            addRow(arrayOf("1", "David", "22222")) // Same ID, different number
        }

        ShadowContentResolver.registerProviderInternal(
            uri.authority!!,
            object : ContentProvider() {
                override fun onCreate() = true
                override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?
                ) = cursor

                override fun getType(uri: Uri) = null
                override fun insert(uri: Uri, values: ContentValues?) = null
                override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
                override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
            }
        )

        val result = repository.getContacts().first()

        val expected = listOf(
            Contact("1", "David", "11111"),
            Contact("1", "David", "22222")
        )

        assertEquals(expected, result)
    }
    @Test
    fun `fetchContactsOnce returns correct contact list`() = runTest {
        val columns = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = MatrixCursor(columns).apply {
            addRow(arrayOf("1", "Eve", "10101"))
        }

        ShadowContentResolver.registerProviderInternal(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI.authority!!,
            object : ContentProvider() {
                override fun onCreate() = true
                override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?
                ) = cursor

                override fun getType(uri: Uri) = null
                override fun insert(uri: Uri, values: ContentValues?) = null
                override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
                override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
            }
        )

        val result = repository.fetchContactsOnce()

        assertEquals(listOf(Contact("1", "Eve", "10101")), result)
    }

}
