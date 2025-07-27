package com.android4you.contactsprovider.presentation

import app.cash.turbine.test
import com.android4you.contactsprovider.data.repository.FakeContactRepository
import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.usecase.GetContactsUseCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModelUsingFakeTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ContactViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadContacts emits success with fake contacts`() = runTest {
        val contacts = listOf(
            Contact("1", "Alice", "123"),
            Contact("2", "Bob", "456")
        )
        val fakeRepo = FakeContactRepository(contacts = contacts)
        val useCase = GetContactsUseCase(fakeRepo)
        viewModel = ContactViewModel(useCase)

        viewModel.loadContacts()

        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())
            assertEquals(ContactUiState.Success(contacts), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadContacts emits error with fake failure`() = runTest {
        val fakeRepo = FakeContactRepository(shouldFail = true)
        val useCase = GetContactsUseCase(fakeRepo)
        viewModel = ContactViewModel(useCase)

        viewModel.loadContacts()

        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is ContactUiState.Error)
            assertEquals("Repository failure", (result as ContactUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
