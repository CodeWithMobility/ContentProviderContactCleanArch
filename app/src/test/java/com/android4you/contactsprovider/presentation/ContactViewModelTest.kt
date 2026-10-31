package com.android4you.contactsprovider.presentation

import app.cash.turbine.test
import com.android4you.contactsprovider.data.repository.FakeContactRepository
import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.usecase.ObserveContactsUseCase
import com.android4you.contactsprovider.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeContactRepository()
    private val viewModel = ContactViewModel(ObserveContactsUseCase(repository))

    @Test
    fun `starts in Loading until the permission state is known`() = runTest {
        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `denied permission shows PermissionRequired`() = runTest {
        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())

            viewModel.onPermissionResult(false)

            assertEquals(ContactUiState.PermissionRequired, awaitItem())
        }
    }

    @Test
    fun `granted permission shows contacts`() = runTest {
        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())

            viewModel.onPermissionResult(true)

            assertEquals(
                ContactUiState.Success(FakeContactRepository.defaultContacts),
                awaitItem()
            )
        }
    }

    @Test
    fun `granted permission with no contacts shows empty Success`() = runTest {
        val vm = ContactViewModel(ObserveContactsUseCase(FakeContactRepository(emptyList())))

        vm.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())

            vm.onPermissionResult(true)

            assertEquals(ContactUiState.Success(emptyList()), awaitItem())
        }
    }

    @Test
    fun `new contacts from the repository update the state`() = runTest {
        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())
            viewModel.onPermissionResult(true)
            assertEquals(
                ContactUiState.Success(FakeContactRepository.defaultContacts),
                awaitItem()
            )

            val updated = listOf(Contact("3", "Alice", listOf("555")))
            repository.emit(updated)

            assertEquals(ContactUiState.Success(updated), awaitItem())
        }
    }

    @Test
    fun `repository failure shows Error`() = runTest {
        repository.error = RuntimeException("Contact fetch failed")

        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())

            viewModel.onPermissionResult(true)

            assertEquals(ContactUiState.Error, awaitItem())
        }
    }

    @Test
    fun `SecurityException maps to PermissionRequired`() = runTest {
        repository.error = SecurityException("READ_CONTACTS revoked")

        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())

            viewModel.onPermissionResult(true)

            assertEquals(ContactUiState.PermissionRequired, awaitItem())
        }
    }

    @Test
    fun `retry reloads after an error`() = runTest {
        repository.error = RuntimeException("boom")

        viewModel.uiState.test {
            assertEquals(ContactUiState.Loading, awaitItem())
            viewModel.onPermissionResult(true)
            assertEquals(ContactUiState.Error, awaitItem())

            repository.error = null
            viewModel.retry()

            // The intermediate Loading state may be conflated away, so only the final state is asserted.
            assertEquals(
                ContactUiState.Success(FakeContactRepository.defaultContacts),
                expectMostRecentItem()
            )
        }
    }
}
