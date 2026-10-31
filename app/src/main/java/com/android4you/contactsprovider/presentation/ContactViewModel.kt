package com.android4you.contactsprovider.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android4you.contactsprovider.domain.usecase.ObserveContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    observeContacts: ObserveContactsUseCase
) : ViewModel() {

    /** null = the UI has not reported the READ_CONTACTS permission state yet. */
    private val permissionGranted = MutableStateFlow<Boolean?>(null)
    private val retryTick = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ContactUiState> =
        combine(permissionGranted, retryTick) { granted, _ -> granted }
            .flatMapLatest { granted ->
                when (granted) {
                    null -> flowOf(ContactUiState.Loading)
                    false -> flowOf(ContactUiState.PermissionRequired)
                    true -> observeContacts()
                        .map<_, ContactUiState> { ContactUiState.Success(it) }
                        .onStart { emit(ContactUiState.Loading) }
                        .catch { e ->
                            // Permission revoked while the app is running.
                            emit(
                                if (e is SecurityException) ContactUiState.PermissionRequired
                                else ContactUiState.Error
                            )
                        }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ContactUiState.Loading
            )

    fun onPermissionResult(granted: Boolean) {
        permissionGranted.value = granted
    }

    fun retry() {
        retryTick.value++
    }
}
