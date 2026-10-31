package com.android4you.contactsprovider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.android4you.contactsprovider.presentation.ContactRoute
import com.android4you.contactsprovider.ui.theme.ContactsProviderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactsProviderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ContactRoute(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
