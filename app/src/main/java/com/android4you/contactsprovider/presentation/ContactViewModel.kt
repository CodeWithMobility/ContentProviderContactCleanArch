package com.android4you.contactsprovider.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.domain.usecase.GetContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ContactViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactUiState>(ContactUiState.Loading)
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    /***
     *
     * If you're okay collecting immediately on init {} (and assume permission is already granted), you can remove loadContacts():

    init {
        viewModelScope.launch {
            getContactsUseCase()
                .onStart { _uiState.value = ContactUiState.Loading }
                .catch { e -> _uiState.value = ContactUiState.Error(e.message ?: "Unknown error") }
                .collect { contacts ->
                    _uiState.value = ContactUiState.Success(contacts)
                }
        }
    }
     *
     */

    /***
     * Use loadContacts() if:
     *
     * You want explicit control (e.g., only start collection after permission)
     *
     * You may retry, or reload data later
     *
     * You manage state via sealed class
     */

    private var loadJob: Job? = null

    fun loadContacts() {
        loadJob?.cancel() // avoid multiple collectors

        loadJob = viewModelScope.launch {
            getContactsUseCase()
                .onStart {
                    _uiState.value = ContactUiState.Loading
                }
                .catch { e ->
                    _uiState.value = ContactUiState.Error(e.message ?: "Unknown error")
                }
                .collect { contacts ->
                    _uiState.value = ContactUiState.Success(contacts)
                }
        }
    }
}
