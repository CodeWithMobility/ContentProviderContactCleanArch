package com.android4you.contactsprovider.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.loadContacts()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.loadContacts()
        } else if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val contacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    when {
        isLoading -> CircularProgressIndicator()

        !hasPermission -> {
            Column {
                Text("Permission required to access contacts.")
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }) {
                    Text("Grant Permission")
                }
            }
        }

        error != null -> {
            Column(Modifier.padding(16.dp)) {
                Text(error ?: "Something went wrong.")
                Button(onClick = { viewModel.loadContacts() }) {
                    Text("Retry")
                }
            }
        }

        contacts.isEmpty() -> Text("No contacts found.")

        else -> LazyColumn {
            items(contacts) { contact ->
                Column(Modifier.padding(16.dp)) {
                    Text(contact.name ?: "Unknown contact")
                    Text(contact.phoneNumber ?: "No Number")
                }
            }
        }
    }
}

