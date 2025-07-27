package com.android4you.contactsprovider.presentation

import app.cash.turbine.test
import com.android4you.contactsprovider.data.repository.FakeContactRepository
import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.repository.ContactRepository
import com.android4you.contactsprovider.domain.usecase.GetContactsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ContactViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getContactsUseCase: GetContactsUseCase
    private lateinit var viewModel: ContactViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getContactsUseCase = mockk()
        viewModel = ContactViewModel(getContactsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadContacts emits Success state with contact list`() = runTest {
        // Given
        val mockContacts = listOf(
            Contact(id = "1", name = "Alice", phoneNumber = "123"),
            Contact(id = "2", name = "Bob", phoneNumber = "456")
        )
        coEvery { getContactsUseCase() } returns flowOf(mockContacts)

        // When
        viewModel.loadContacts()

        // Then
        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())
            assertEquals(ContactUiState.Success(mockContacts), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadContacts emits Error state when exception occurs`() = runTest {
        val error = RuntimeException("Something went wrong")
        coEvery { getContactsUseCase() } returns flow {
            throw error
        }

        viewModel.loadContacts()

        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is ContactUiState.Error)
            assertEquals("Something went wrong", (result as ContactUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

