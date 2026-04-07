package com.ghuba.taprux.ui.pages.track

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.ghuba.taprux.R
import com.ghuba.taprux.core.TrackableModel
import com.ghuba.taprux.toByteArray

@Composable
fun TrackPage(trackables: List<TrackableModel>) {
  TrackableList(trackables = trackables)
}

@Composable
fun TrackableList(trackables: List<TrackableModel>) {
  val sortedTrackables = trackables.sortedBy { it.hasSubEvents }

  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(1.dp),
      verticalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    items(sortedTrackables, key = { it.id.toLong() }) { trackable -> TrackableItem(trackable) }
  }
}

@Composable
fun TrackableItem(trackable: TrackableModel) {
  val context = LocalContext.current
  val svgData = remember(trackable.svgIcon) { trackable.svgIcon.toByteArray() }

  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(1.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
  ) {
    Row(modifier = Modifier.padding(1.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
          modifier = Modifier.size(64.dp).padding(end = 1.dp),
          contentAlignment = Alignment.Center,
      ) {
        AsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(svgData)
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
            contentDescription = trackable.name,
            modifier = Modifier.defaultMinSize(66.dp, 66.dp),
        )
      }

      Text(
          text = trackable.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
      )

      Box(
          modifier = Modifier.size(32.dp).padding(start = 1.dp),
          contentAlignment = Alignment.Center,
      ) {
        Box(
            modifier = Modifier.background(Color.Blue, CircleShape).size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
          Text(
              text = trackable.eventOccurrence.toString(),
              color = Color.White,
              style = MaterialTheme.typography.bodySmall,
          )
        }
      }

      if (trackable.hasSubEvents) {
        AsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(R.drawable.ic_chevron_right)
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
            contentDescription = trackable.name,
            modifier = Modifier.size(36.dp).padding(start = 8.dp, end = 8.dp),
        )
      }
    }
  }
}
