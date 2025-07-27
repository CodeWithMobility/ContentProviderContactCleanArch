package com.android4you.contactsprovider.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
@Composable
fun ContactScreen(viewModel: ContactViewModel = hiltViewModel()) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher for requesting permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            viewModel.loadContacts()
        }
    }

    // Trigger permission request when Composable launches
    LaunchedEffect(Unit) {
        if (hasPermission) {
            viewModel.loadContacts()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    when {
        !hasPermission -> {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Permission required to read contacts.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }) {
                    Text("Grant Permission")
                }
            }
        }

        uiState is ContactUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState is ContactUiState.Success -> {
            val contacts = (uiState as ContactUiState.Success).contacts
            if (contacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No contacts found.")
                }
            } else {
                LazyColumn {
                    items(contacts) { contact ->
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = contact.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = contact.phoneNumber ?: "No number")
                        }
                    }
                }
            }
        }

        uiState is ContactUiState.Error -> {
            val message = (uiState as ContactUiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $message", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
