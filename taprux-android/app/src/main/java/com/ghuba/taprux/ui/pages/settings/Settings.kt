package com.ghuba.taprux.ui.pages.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghuba.taprux.core.ApplicationSettings
import com.ghuba.taprux.core.WeekDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: ApplicationSettings,
    onFirstRun: () -> Unit,
    onBackClick: () -> Unit,
    onSettingsChange: (ApplicationSettings) -> Unit,
    onExportCsv: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onRemoveAccess: () -> Unit,
    onResetEverything: () -> Unit,
    onCancelAccount: () -> Unit,
) {
  LaunchedEffect(Unit) { onFirstRun() }

  // Dialog visibility states
  var showTimezoneConfirm by remember { mutableStateOf(false) }
  var showRestoreConfirm by remember { mutableStateOf(false) }
  var showRemoveInsightsConfirm by remember { mutableStateOf(false) }
  var showResetEverythingConfirm by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
              IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to track view")
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        )
      },
      containerColor = Color.White,
  ) { paddingValues ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
      // APP SETTINGS
      SettingsSection(title = "App Settings") {
        // Week Start Day Dropdown
        var expanded by remember { mutableStateOf(false) }
        Column(modifier = Modifier.fillMaxWidth()) {
          Text("Week Starts On", fontWeight = FontWeight.Medium, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(8.dp))
          ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = state.weekStartDay.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(8.dp),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
              DropdownMenuItem(
                  text = { Text("Sunday") },
                  onClick = {
                    onSettingsChange(state.copy(weekStartDay = WeekDay.SUNDAY))
                    expanded = false
                  },
              )
              DropdownMenuItem(
                  text = { Text("Monday") },
                  onClick = {
                    onSettingsChange(state.copy(weekStartDay = WeekDay.MONDAY))
                    expanded = false
                  },
              )
            }
          }
          Text(
              "Changes how weeks are displayed in trend views",
              color = Color.Gray,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp),
          )
        }

        // Show Trackable Names
        SettingToggle(
            title = "Show trackable names",
            subtitle = "Display names below icons on the tracking screen",
            checked = state.showTrackableNames,
            onCheckedChange = { onSettingsChange(state.copy(showTrackableNames = it)) },
        )

        // Timezone
        Column(modifier = Modifier.fillMaxWidth()) {
          Text("Home Timezone", fontWeight = FontWeight.Medium, fontSize = 15.sp)
          Text(state.homeTimezone, fontWeight = FontWeight.Medium)

          if (state.homeTimezone != state.deviceTimezone) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            ) {
              Text(
                  text = "Your device is currently in ${state.deviceTimezone}.",
                  color = MaterialTheme.colorScheme.primary,
                  fontSize = 13.sp,
                  modifier = Modifier.padding(8.dp),
              )
            }
          }

          OutlinedButton(
              onClick = { showTimezoneConfirm = true },
              modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
              shape = RoundedCornerShape(8.dp),
          ) {
            Text("Update to current location")
          }
          Text(
              "Your home timezone controls how days and weeks are displayed. Update this if you've relocated.",
              color = Color.Gray,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp),
          )
        }
      }

      // NOTIFICATIONS
      SettingsSection(title = "Notifications") {
        SettingToggle(
            title = "Week in Wellness reports",
            subtitle = "New wellness report is ready",
            checked = state.notifInsightsReports,
            onCheckedChange = { onSettingsChange(state.copy(notifInsightsReports = it)) },
        )
        SettingToggle(
            title = "Health HERstory",
            subtitle = "Reminder to complete your health background",
            checked = state.notifHealthHerstory,
            onCheckedChange = { onSettingsChange(state.copy(notifHealthHerstory = it)) },
        )
      }

      // PARTNER SECTION PLACEHOLDER
      // PartnerSection()

      // DATA MANAGEMENT
      SettingsSection(title = "Data Management") {
        SettingButton(
            title = "Export to CSV",
            subtitle = "Download all your tracking data as a CSV file",
            onClick = onExportCsv,
        )
        SettingButton(
            title = "Create Backup",
            subtitle = "Save a complete backup of your database",
            onClick = onCreateBackup,
        )
        SettingButton(
            title = "Restore from Backup",
            subtitle = "Replace all data with a backup file (requires restart)",
            onClick = { showRestoreConfirm = true },
        )
      }

      // RESET TOOLS
      SettingsSection(title = "Reset Tools") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Button(
              onClick = { showRemoveInsightsConfirm = true },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
              shape = RoundedCornerShape(8.dp),
          ) {
            Text("Remove access")
          }
          Button(
              onClick = { showResetEverythingConfirm = true },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
              shape = RoundedCornerShape(8.dp),
          ) {
            Text("Delete Everything")
          }
        }
      }

      // STATUS MESSAGE
      state.statusMessage?.let { msg ->
        Surface(
            color = Color(0xFFF8F9FA),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
              text = msg,
              modifier = Modifier.padding(12.dp),
              textAlign = TextAlign.Center,
              color = Color.DarkGray,
          )
        }
      }

      // CANCEL ACCOUNT
      if (state.isInsightsActivated && state.hasAccess && !state.isTrial) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Text(
              text = "Account Cancellation Process",
              color = Color.Gray,
              fontSize = 13.sp,
              textDecoration = TextDecoration.Underline,
              modifier = Modifier.clickable { onCancelAccount() },
          )
        }
      }

      // FOOTER PLACEHOLDER
      // SettingsFooter(version = state.appVersion)
      Text(
          text = "Version ${state.appVersion}",
          color = Color.LightGray,
          fontSize = 12.sp,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(32.dp))
    }
  }

  // --- DIALOGS ---

  if (showTimezoneConfirm) {
    AlertDialog(
        onDismissRequest = { showTimezoneConfirm = false },
        title = { Text("Update Timezone?") },
        text = {
          Text(
              "Change your home timezone to ${state.deviceTimezone}? This changes how your days and weeks are displayed going forward."
          )
        },
        confirmButton = {
          Button(
              onClick = {
                onSettingsChange(state)
                showTimezoneConfirm = false
              }
          ) {
            Text("Update")
          }
        },
        dismissButton = {
          TextButton(onClick = { showTimezoneConfirm = false }) { Text("Cancel") }
        },
    )
  }

  if (showRestoreConfirm) {
    AlertDialog(
        onDismissRequest = { showRestoreConfirm = false },
        title = { Text("Restore Backup") },
        text = {
          Text("This will replace ALL data with the backup file and restart the app. Continue?")
        },
        confirmButton = {
          Button(
              onClick = {
                onRestoreBackup()
                showRestoreConfirm = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
          ) {
            Text("Yes, Restore Backup")
          }
        },
        dismissButton = { TextButton(onClick = { showRestoreConfirm = false }) { Text("Cancel") } },
    )
  }

  if (showRemoveInsightsConfirm) {
    AlertDialog(
        onDismissRequest = { showRemoveInsightsConfirm = false },
        title = { Text("Remove Access") },
        text = {
          Text("Remove access? Your tracking data stays. You can re-enter a code any time.")
        },
        confirmButton = {
          Button(
              onClick = {
                onRemoveAccess()
                showRemoveInsightsConfirm = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
          ) {
            Text("Yes, Remove access")
          }
        },
        dismissButton = {
          TextButton(onClick = { showRemoveInsightsConfirm = false }) { Text("Cancel") }
        },
    )
  }

  if (showResetEverythingConfirm) {
    AlertDialog(
        onDismissRequest = { showResetEverythingConfirm = false },
        title = { Text("Delete Everything") },
        text = {
          Text(
              "Delete all trackables, counts, and wellness report data? This will clear your access and trigger onboarding. This cannot be undone."
          )
        },
        confirmButton = {
          Button(
              onClick = {
                onResetEverything()
                showResetEverythingConfirm = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
          ) {
            Text("Yes, Delete Everything")
          }
        },
        dismissButton = {
          TextButton(onClick = { showResetEverythingConfirm = false }) { Text("Cancel") }
        },
    )
  }
}

// --- HELPER COMPOSABLES ---

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF333333),
        modifier = Modifier.padding(bottom = 16.dp),
    )
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) { content() }
    HorizontalDivider(
        modifier = Modifier.padding(top = 24.dp),
        thickness = DividerDefaults.Thickness,
        color = Color(0xFFF0F0F0),
    )
  }
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
      Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    Text(subtitle, color = Color.Gray, fontSize = 13.sp)
  }
}

@Composable
fun SettingButton(title: String, subtitle: String, onClick: () -> Unit) {
  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors =
            ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
    ) {
      Text(title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
    }
    Text(subtitle, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
  }
}
