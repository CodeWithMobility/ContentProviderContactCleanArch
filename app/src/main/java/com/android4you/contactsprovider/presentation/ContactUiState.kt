package com.android4you.contactsprovider.presentation

import com.android4you.contactsprovider.domain.model.Contact

sealed interface ContactUiState {
    data object Loading : ContactUiState
    data object PermissionRequired : ContactUiState
    data object Error : ContactUiState
    data class Success(val contacts: List<Contact>) : ContactUiState
}
