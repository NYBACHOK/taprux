package com.ghuba.taprux.ui.pages.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghuba.taprux.core.TrackableModel
import com.ghuba.taprux.ui.components.TabItem

enum class EditCurrentPage {
  Library,
  Manage,
}

@Composable
fun EditPage(
    trackables: List<TrackableModel>,
) {
  val activePage = remember { mutableStateOf(EditCurrentPage.Library) }

  Column(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.weight(1f)) {
      when (activePage.value) {
        EditCurrentPage.Library -> LibraryTrackablesPage()
        EditCurrentPage.Manage ->
            ManageTrackablesPage(
                trackables = trackables,
                onBackClick = {},
                onAddClick = {},
                onEditClick = {},
                onDeleteTrackable = {},
                onReorderTrackables = {},
            )
      }

      TabBar(activePage = activePage.value, onPageSelected = { activePage.value = it })
    }
  }
}

@Composable
fun TabBar(activePage: EditCurrentPage, onPageSelected: (EditCurrentPage) -> Unit) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp,
    ) {
      TabItem(
          page = EditCurrentPage.Library,
          icon = Icons.AutoMirrored.Filled.LibraryBooks,
          isActive = activePage == EditCurrentPage.Library,
          onSelect = onPageSelected,
      )
      TabItem(
          page = EditCurrentPage.Manage,
          icon = Icons.Default.AddToPhotos,
          isActive = activePage == EditCurrentPage.Manage,
          onSelect = onPageSelected,
      )
    }
  }
}
