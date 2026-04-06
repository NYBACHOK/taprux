package com.ghuba.taprux

import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.ghuba.taprux.core.Event
import com.ghuba.taprux.core.QueryRequest
import com.ghuba.taprux.core.TrackableModel
import com.ghuba.taprux.core.ViewModel
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
  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
      modifier = Modifier.fillMaxSize(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
          modifier = Modifier
              .size(64.dp)
              .padding(end = 16.dp),
          contentAlignment = Alignment.Center) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(svgData)
                .decoderFactory(SvgDecoder.Factory())
                .crossfade(true)
                .build(),
            contentDescription = trackable.name,
            modifier = Modifier.size(56.dp))
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = trackable.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = "Occurrences: ${trackable.eventOccurrence}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp))
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
