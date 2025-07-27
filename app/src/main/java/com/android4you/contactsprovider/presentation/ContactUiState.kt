package com.android4you.contactsprovider.presentation

import com.android4you.contactsprovider.domain.model.Contact

sealed class ContactUiState {
    object Loading : ContactUiState()
    data class Success(val contacts: List<Contact>) : ContactUiState()
    data class Error(val message: String) : ContactUiState()
}

