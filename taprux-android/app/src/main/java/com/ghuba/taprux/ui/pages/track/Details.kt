package com.ghuba.taprux.ui.pages.track

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghuba.taprux.core.TrackableWithChildrenModel
import com.ghuba.taprux.ui.components.TrackableGridItem

@Composable
fun DetailsPage(
    details: TrackableWithChildrenModel,
    todayCounts: Map<Int, Int>,
    showNames: Boolean,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
) {
  Column(
      modifier =
          Modifier.fillMaxSize()
              .background(MaterialTheme.colorScheme.background)
              .verticalScroll(rememberScrollState())
              .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
        text = details.name,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
    )

    // Children Trackables Section
    if (details.subEvents.isNotEmpty()) {
      LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          contentPadding = PaddingValues(0.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.height(200.dp),
          userScrollEnabled = false,
      ) {
        items(details.subEvents, key = { it.id.toInt() }) { trackable ->
          val id = trackable.id.toInt()
          val count = todayCounts[id] ?: 0
          TrackableGridItem(
              trackable = trackable,
              count = count,
              showName = showNames,
              onClick = { onIncrement(id) },
              onDoubleClick = { onDecrement(id) },
              onLongClick = {},
          )
        }
      }
    } else {
      Text(
          text = "No trackables",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
