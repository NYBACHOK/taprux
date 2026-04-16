package com.ghuba.taprux.ui.pages.edit

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ghuba.taprux.core.TrackableModel
import com.ghuba.taprux.ui.components.TrackableGridItem

@Composable
fun LibraryTrackablesPage(
    trackables: List<TrackableModel>,
    onAddUserTrackable: (Int) -> Unit,
    showNames: Boolean,
) {
  val columns = 3
  val rows = 4
  val itemsPerPage = columns * rows

  val pages = trackables.chunked(itemsPerPage)
  val pagerState = rememberPagerState(pageCount = { pages.size })

  val context = LocalContext.current

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(modifier = Modifier.fillMaxSize()) {
      HorizontalPager(
          state = pagerState,
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.Top,
      ) { pageIndex ->
        val pageItems = pages[pageIndex]

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
          items(pageItems, key = { it.id.toInt() }) { trackable ->
            val id = trackable.id.toInt()

            val toast = Toast.makeText(context, "Long press to add trackable", Toast.LENGTH_SHORT)

            TrackableGridItem(
                trackable = trackable,
                showName = showNames,
                onClick = { toast.show() },
                onDoubleClick = { toast.show() },
                onLongClick = { onAddUserTrackable(id) },
            )
          }
        }
      }

      // Pagination Dots
      if (pages.size > 1) {
        Row(Modifier.height(40.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          repeat(pages.size) { iteration ->
            val color =
                if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant

            val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp

            Box(
                modifier =
                    Modifier.padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                        .size(width = width, height = 8.dp)
            )
          }
        }
      }
    }
  }
}
