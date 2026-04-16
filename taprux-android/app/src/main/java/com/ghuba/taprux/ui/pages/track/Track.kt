package com.ghuba.taprux.ui.pages.track

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghuba.taprux.core.TrackableModel
import com.ghuba.taprux.core.TrackableWithChildrenModel
import com.ghuba.taprux.ui.components.TabItem

enum class TrackCurrentPage {
  Trackables,
  Details,
}

@Composable
fun TrackPage(
    trackables: List<TrackableModel>,
    todayCounts: Map<Int, Int>,
    showNames: Boolean,
    details: TrackableWithChildrenModel?,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onNavigateToDetails: (Int) -> Unit,
) {
  val activePage = remember { mutableStateOf(TrackCurrentPage.Trackables) }

  // When navigating to details, switch to Details page
  // This is a bit hacky, but since onNavigateToDetails is called when details is set,
  // we can assume it's to show details
  if (details != null && activePage.value == TrackCurrentPage.Trackables) {
    activePage.value = TrackCurrentPage.Details
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.weight(1f)) {
      when (activePage.value) {
        TrackCurrentPage.Trackables ->
            ListPage(
                trackables = trackables,
                todayCounts = todayCounts,
                showNames = showNames,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onNavigateToDetails = {
                  onNavigateToDetails(it)
                  activePage.value = TrackCurrentPage.Details
                },
            )
        TrackCurrentPage.Details ->
            details?.let { detailsModel ->
              DetailsPage(
                  details = detailsModel,
                  allTrackables = trackables, // assuming allTrackables is trackables here
                  todayCounts = todayCounts,
                  showNames = showNames,
                  onIncrement = onIncrement,
                  onDecrement = onDecrement,
                  onNavigateToDetails = onNavigateToDetails,
              )
            }
                ?: run {
                  // If details is null, show a message or switch back
                  Text(
                      "No details available",
                      modifier = Modifier.align(Alignment.Center),
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
      }

      if (details != null) {
        TrackTabBar(
            activePage = activePage.value,
            onPageSelected = { activePage.value = it },
        )
      }
    }
  }
}

@Composable
fun TrackTabBar(
    activePage: TrackCurrentPage,
    onPageSelected: (TrackCurrentPage) -> Unit,
) {

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp,
    ) {
      TabItem(
          page = TrackCurrentPage.Trackables,
          icon = Icons.AutoMirrored.Filled.List,
          isActive = activePage == TrackCurrentPage.Trackables,
          onSelect = onPageSelected,
      )

      TabItem(
          page = TrackCurrentPage.Details,
          icon = Icons.Default.Info,
          isActive = activePage == TrackCurrentPage.Details,
          onSelect = onPageSelected,
      )
    }
  }
}
