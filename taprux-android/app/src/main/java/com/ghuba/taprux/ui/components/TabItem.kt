package com.ghuba.taprux.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun <T : Enum<T>> RowScope.TabItem(
  page: T,
  label : String? = null,
  icon: ImageVector,
  isActive: Boolean,
  onSelect: (T) -> Unit,
) {
  NavigationBarItem(
    selected = isActive,
    onClick = { onSelect(page) },
    icon = { Icon(imageVector = icon, contentDescription = page.name) },
    label =  { Text(text = label ?: page.name, style = MaterialTheme.typography.labelMedium) },
    colors =
      NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
      ),
  )
}