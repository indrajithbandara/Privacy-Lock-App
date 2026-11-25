package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ProtectionProfile
import com.example.security.AppLockManager
import com.example.ui.PrivacyViewModel
import com.example.ui.components.SecureAuthDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfilesScreen(viewModel: PrivacyViewModel) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val members by viewModel.profileMembers.collectAsStateWithLifecycle()
    val apps by viewModel.allApps.collectAsStateWithLifecycle()
    val config by viewModel.securityConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val enrolled = config?.hashedPin?.isNotEmpty() == true
    var eligible by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(apps) {
        eligible = withContext(Dispatchers.IO) { apps.filter { AppLockManager.canProtect(context, it.packageName) }.map { it.packageName }.toSet() }
    }
    var editing by remember { mutableStateOf<ProtectionProfile?>(null) }
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(emptySet<String>()) }
    var immediateRelock by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    var deleting by remember { mutableStateOf<ProtectionProfile?>(null) }
    var error by remember { mutableStateOf(false) }
    val result: (Boolean) -> Unit = { error = !it; if (it) editing = null }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 20.dp)) {
        item {
            Text(stringResource(R.string.nav_profiles), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.profiles_explanation))
            if (!enrolled) Text(stringResource(R.string.settings_pin_first))
            if (error) Text(stringResource(R.string.profile_error), color = MaterialTheme.colorScheme.error)
            Button(enabled = enrolled, onClick = { editing = ProtectionProfile(name = ""); name = ""; selected = emptySet(); immediateRelock = false }) { Text(stringResource(R.string.profile_create)) }
        }
        items(profiles, key = { it.id }) { profile ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(profile.name, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.profile_app_count, members.count { it.profileId == profile.id }))
                            Text(if (profile.active) stringResource(R.string.profile_active) else stringResource(R.string.profile_inactive))
                            if (profile.forceImmediateRelock) Text(stringResource(R.string.profile_immediate_relock), style = MaterialTheme.typography.bodySmall)
                        }
                        val switchLabel = stringResource(R.string.profile_switch, profile.name)
                        Switch(checked = profile.active, enabled = enrolled, modifier = Modifier.semantics { contentDescription = switchLabel }, onCheckedChange = { active -> pending = { viewModel.setProfileActive(profile.id, active, result) } })
                    }
                    Row {
                        TextButton(enabled = enrolled, onClick = { editing = profile; name = profile.name; immediateRelock = profile.forceImmediateRelock; selected = members.filter { it.profileId == profile.id }.map { it.packageName }.toSet() }) { Text(stringResource(R.string.profile_edit)) }
                        TextButton(enabled = enrolled, onClick = { deleting = profile }) { Text(stringResource(R.string.profile_delete)) }
                    }
                }
            }
        }
    }
    editing?.let { profile ->
        if (pending == null) AlertDialog(onDismissRequest = { editing = null }, title = { Text(stringResource(R.string.profile_edit)) }, text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it.take(60) }, label = { Text(stringResource(R.string.profile_name)) }, singleLine = true)
                Row(Modifier.fillMaxWidth().toggleable(immediateRelock, role = Role.Checkbox, onValueChange = { immediateRelock = it }).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = immediateRelock, onCheckedChange = null)
                    Text(stringResource(R.string.profile_immediate_relock), modifier = Modifier.weight(1f))
                }
                LazyColumn(Modifier.heightIn(max = 280.dp)) {
                    items(apps.filter { it.packageName in eligible || it.packageName in selected }, key = { it.packageName }) { app ->
                        val checked = app.packageName in selected
                        Row(Modifier.fillMaxWidth().toggleable(checked, role = Role.Checkbox, onValueChange = { next -> selected = if (next) selected + app.packageName else selected - app.packageName }).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = checked, onCheckedChange = null)
                            Text(app.name)
                        }
                    }
                }
            }
        }, confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                val savedName = name; val savedSelection = selected; val savedRelock = immediateRelock
                pending = { viewModel.saveProfile(profile.id.takeIf { it != 0L }, savedName, savedSelection, savedRelock, result) }
            }) { Text(stringResource(R.string.common_apply)) }
        }, dismissButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.common_cancel)) } })
    }
    deleting?.let { profile ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text(stringResource(R.string.profile_delete)) }, text = { Text(profile.name) },
            confirmButton = { TextButton(onClick = { deleting = null; pending = { viewModel.deleteProfile(profile.id, result) } }) { Text(stringResource(R.string.common_confirm)) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.common_cancel)) } })
    }
    SecureAuthDialog(isOpen = pending != null, title = stringResource(R.string.confirm_protection_change), randomizeKeypad = config?.randomizeKeypad == true,
        isPinConfigured = enrolled, onVerify = viewModel::verifyPin, onDismiss = { pending = null }, onSuccess = {
            val action = pending; pending = null; action?.invoke()
        })
}
