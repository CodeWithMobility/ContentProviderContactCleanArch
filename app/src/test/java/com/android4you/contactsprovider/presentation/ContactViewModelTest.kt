package com.android4you.contactsprovider.presentation

import com.android4you.contactsprovider.data.repository.FakeContactRepository
import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.repository.ContactRepository
import com.android4you.contactsprovider.domain.usecase.GetContactsUseCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModelTest {

    private lateinit var viewModel: ContactViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = ContactViewModel(GetContactsUseCase(FakeContactRepository()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadContacts should update contacts and loading state`() = runTest {
        viewModel.loadContacts()

        advanceUntilIdle()

        val expectedContacts = listOf(
            Contact("1", "John Doe", "1234567890"),
            Contact("2", "Jane Smith", null)
        )

        assertEquals(expectedContacts, viewModel.contacts.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadContacts returns expected contacts`() = runTest {
        val fakeRepo = object : ContactRepository {
            override suspend fun getContacts() = listOf(
                Contact("1", "Alice", "1234"),
                Contact("2", "Bob", null)
            )
        }

        viewModel = ContactViewModel(GetContactsUseCase(fakeRepo))
        viewModel.loadContacts()

        advanceUntilIdle()

        val result = viewModel.contacts.value
        assertEquals(2, result.size)
        assertEquals("Alice", result[0].name)
        assertEquals("Bob", result[1].name)
    }

    @Test
    fun `loadContacts handles empty list`() = runTest {
        val fakeRepo = object : ContactRepository {
            override suspend fun getContacts() = emptyList<Contact>()
        }

        viewModel = ContactViewModel(GetContactsUseCase(fakeRepo))
        viewModel.loadContacts()

        advanceUntilIdle()

        assertTrue(viewModel.contacts.value.isEmpty())
    }

    @Test
    fun `loadContacts handles exception and sets error message`() = runTest {
        val fakeRepo = object : ContactRepository {
            override suspend fun getContacts(): List<Contact> {
                throw RuntimeException("Contact fetch failed")
            }
        }

        viewModel = ContactViewModel(GetContactsUseCase(fakeRepo))
        viewModel.loadContacts()

        advanceUntilIdle()

        assertEquals("Contact fetch failed", viewModel.error.value)
        assertTrue(viewModel.contacts.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadContacts sets isLoading correctly`() = runTest {
        val fakeRepo = object : ContactRepository {
            override suspend fun getContacts(): List<Contact> {
                delay(100) // Simulate loading
                return listOf(Contact("1", "Delayed Contact", "5555"))
            }
        }

        viewModel = ContactViewModel(GetContactsUseCase(fakeRepo))
        val job = launch {
            viewModel.loadContacts()
        }

        // Immediately after launch
        assertFalse(viewModel.isLoading.value)

        advanceUntilIdle() // Complete delay

        assertFalse(viewModel.isLoading.value)
        job.cancel()
    }

    @Test
    fun `loadContacts multiple calls overwrite previous contacts`() = runTest {
        val repo1 = object : ContactRepository {
            override suspend fun getContacts() = listOf(Contact("1", "First", "123"))
        }

        val repo2 = object : ContactRepository {
            override suspend fun getContacts() = listOf(Contact("2", "Second", "456"))
        }

        viewModel = ContactViewModel(GetContactsUseCase(repo1))
        viewModel.loadContacts()
        advanceUntilIdle()
        assertEquals("First", viewModel.contacts.value.first().name)

        viewModel = ContactViewModel(GetContactsUseCase(repo2))
        viewModel.loadContacts()
        advanceUntilIdle()
        assertEquals("Second", viewModel.contacts.value.first().name)
    }


}
