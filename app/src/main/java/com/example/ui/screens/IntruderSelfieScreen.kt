package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.PrivacyViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun IntruderSelfieScreen(
    viewModel: PrivacyViewModel,
    modifier: Modifier = Modifier
) {
    val timeline by viewModel.timelineEvents.collectAsStateWithLifecycle()
    val events = timeline.filter { it.type == "AUTH_FAILURE" || it.type == "AUTH_LOCKOUT" }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.security_events), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.events_privacy),
            style = MaterialTheme.typography.bodyMedium)
        if (events.isEmpty()) {
            Text(stringResource(R.string.no_auth_events))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(events, key = { it.id }) { event ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(event.appName ?: stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                            Text(event.description, style = MaterialTheme.typography.bodyMedium)
                            Text(DateFormat.getDateTimeInstance().format(Date(event.timestamp)),
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
