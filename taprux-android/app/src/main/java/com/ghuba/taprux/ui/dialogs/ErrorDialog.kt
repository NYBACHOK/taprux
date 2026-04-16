package com.ghuba.taprux.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(
    title: String = "Error",
    message: String,
    isCritical: Boolean = false,
    onDismiss: () -> Unit,
    onRestart: () -> Unit,
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
      text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
      confirmButton = {
        Button(onClick = onRestart) { Text(if (isCritical) "Restart App" else "Try Again") }
      },
      dismissButton = { Button(onClick = onDismiss) { Text("Close") } },
  )
}
