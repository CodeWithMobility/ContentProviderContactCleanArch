package com.android4you.contactsprovider.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android4you.contactsprovider.R
import com.android4you.contactsprovider.domain.model.Contact
import com.android4you.contactsprovider.ui.theme.ContactsProviderTheme

/**
 * Stateful entry point: owns the permission handling and talks to the ViewModel.
 * All rendering lives in the stateless [ContactScreen].
 */
@Composable
fun ContactRoute(
    modifier: Modifier = Modifier,
    viewModel: ContactViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var permanentlyDenied by remember { mutableStateOf(false) }
    var alreadyRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
        // After a denial, "should show rationale" is false only when the system will no longer
        // show the dialog, so the user has to go through the app settings.
        permanentlyDenied = !granted && !context.shouldShowContactsRationale()
    }

    // Runs on first composition and every time the user comes back to the app,
    // e.g. after toggling the permission in the system settings.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val granted = context.hasContactsPermission()
        viewModel.onPermissionResult(granted)
        if (granted) {
            permanentlyDenied = false
        } else if (!alreadyRequested) {
            alreadyRequested = true
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    ContactScreen(
        state = state,
        permanentlyDenied = permanentlyDenied,
        onPermissionAction = {
            if (permanentlyDenied) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
            } else {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        },
        onRetry = viewModel::retry,
        modifier = modifier
    )
}

@Composable
fun ContactScreen(
    state: ContactUiState,
    permanentlyDenied: Boolean,
    onPermissionAction: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        ContactUiState.Loading -> CenteredColumn(modifier) {
            CircularProgressIndicator()
        }

        ContactUiState.PermissionRequired -> CenteredColumn(modifier) {
            Text(
                text = stringResource(
                    if (permanentlyDenied) R.string.permission_denied_permanently
                    else R.string.permission_required
                ),
                textAlign = TextAlign.Center
            )
            Button(onClick = onPermissionAction) {
                Text(
                    stringResource(
                        if (permanentlyDenied) R.string.open_settings else R.string.grant_permission
                    )
                )
            }
        }

        ContactUiState.Error -> CenteredColumn(modifier) {
            Text(text = stringResource(R.string.error_generic), textAlign = TextAlign.Center)
            Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }

        is ContactUiState.Success -> if (state.contacts.isEmpty()) {
            CenteredColumn(modifier) { Text(stringResource(R.string.no_contacts)) }
        } else {
            ContactList(state.contacts, modifier)
        }
    }
}

@Composable
private fun ContactList(contacts: List<Contact>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(items = contacts, key = { it.id }) { contact ->
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = contact.name.ifBlank { stringResource(R.string.unknown_contact) },
                    style = MaterialTheme.typography.titleMedium
                )
                if (contact.phoneNumbers.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_number),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    contact.phoneNumbers.forEach { number ->
                        Text(text = number, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        content()
    }
}

private fun Context.hasContactsPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.shouldShowContactsRationale(): Boolean {
    val activity = findActivity() ?: return false
    return ActivityCompat.shouldShowRequestPermissionRationale(
        activity,
        Manifest.permission.READ_CONTACTS
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Preview(showBackground = true)
@Composable
private fun ContactScreenSuccessPreview() {
    ContactsProviderTheme {
        ContactScreen(
            state = ContactUiState.Success(
                listOf(
                    Contact("1", "John Doe", listOf("+1 555 0100", "+1 555 0101")),
                    Contact("2", "Jane Smith")
                )
            ),
            permanentlyDenied = false,
            onPermissionAction = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContactScreenPermissionPreview() {
    ContactsProviderTheme {
        ContactScreen(
            state = ContactUiState.PermissionRequired,
            permanentlyDenied = true,
            onPermissionAction = {},
            onRetry = {}
        )
    }
}
