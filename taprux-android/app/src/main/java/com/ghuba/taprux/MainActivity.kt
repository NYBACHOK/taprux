package com.ghuba.taprux

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.ghuba.taprux.core.Event
import com.ghuba.taprux.core.QueryRequest
import com.ghuba.taprux.core.TrackableModel
import com.ghuba.taprux.ui.theme.TapruxTheme

class MainActivity : ComponentActivity() {
  private val core = Core()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    createInsets(findViewById<View>(android.R.id.content).rootView)
    setContent {
      TapruxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          View(core)
        }
      }
    }

    core.update(Event.Query(QueryRequest.List))
  }
}

fun createInsets(view: View?) {
  if (view == null) return

  ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
    val imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
    val bottomInsets =
        if (imeVisible) {
          windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        } else {
          windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        }

    val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

    v.setPadding(0, systemBarsInsets.top, 0, bottomInsets)

    windowInsets
  }
}

@Composable
fun View(core: Core) {
  val viewState by core.viewModel.collectAsState()
  TrackableList(trackables = viewState.trackables)
}

@Composable
fun TrackableList(trackables: List<TrackableModel>) {
  LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 200.dp),
      modifier = Modifier.fillMaxSize().padding(1.dp)) {
    items(trackables, key = { it.id.toLong() }) { trackable ->
      TrackableItem(trackable)
    }
  }
}

@Composable
fun TrackableItem(trackable: TrackableModel) {
  val context = LocalContext.current
  val svgData = remember(trackable.svgIcon) { trackable.svgIcon.toByteArray() }

  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(1.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Row(modifier = Modifier.padding(1.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
          modifier = Modifier
              .size(64.dp)
              .padding(end = 1.dp),
          contentAlignment = Alignment.Center) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(svgData)
                .decoderFactory(SvgDecoder.Factory())
                .crossfade(true)
                .build(),
            contentDescription = trackable.name,
            modifier = Modifier.defaultMinSize(66.dp))
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = trackable.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1)
      }

      Box(
          modifier = Modifier
              .size(32.dp)
              .padding(start = 1.dp),
          contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .background(Color.Blue, CircleShape)
                .size(24.dp),
            contentAlignment = Alignment.Center) {
          Text(
              text = trackable.eventOccurrence.toString(),
              color = Color.White,
              style = MaterialTheme.typography.bodySmall)
        }
      }
    }
  }
}

private fun List<UByte>.toByteArray(): ByteArray {
  return ByteArray(size) { index -> get(index).toByte() }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  TapruxTheme {
    TrackableList(
        trackables = listOf(
            TrackableModel(1u, "Work", emptyList(), 4u, false),
            TrackableModel(2u, "Personal", emptyList(), 2u, true)))
  }
}
