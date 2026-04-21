package com.ghuba.taprux.ui.pages.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.ghuba.taprux.core.TrackableModel
import com.ghuba.taprux.toByteArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTrackablesPage(
    trackables: List<TrackableModel>,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteTrackable: (UInt) -> Unit,
    onReorderTrackables: (Map<UInt, UInt>) -> Unit,
) {

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Manage Trackables", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
              IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
        )
      },
      floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = onAddClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(12.dp),
        ) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text("Add Trackable")
        }
      },
  ) { paddingValues ->
    Box(
        modifier =
            Modifier.fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
    ) {
      if (trackables.isEmpty()) {
        Text(
            text = "No trackables yet. Add your first one to get started!",
            modifier = Modifier.align(Alignment.Center).padding(32.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(trackables, key = { it.id.toLong() }) { trackable ->
            TrackableItem(
                trackable = trackable,
                onEdit = { onEditClick(trackable.id.toLong()) },
                onDelete = {
                  // Call onDeleteTrackable(trackable.id)
                },
            )
          }
        }
      }
    }
  }
}

@Composable
fun TrackableItem(trackable: TrackableModel, onEdit: () -> Unit, onDelete: () -> Unit) {
  val context = LocalContext.current

  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
      shape = RoundedCornerShape(8.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
  ) {
    Row(
        modifier = Modifier.padding(12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Drag Handle
      Icon(
          imageVector = Icons.Default.Menu,
          contentDescription = "Reorder",
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
      )

      // Icon Display
      Box(
          modifier =
              Modifier.size(40.dp)
                  .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center,
      ) {
        val svgData = remember(trackable.svgIcon) { trackable.svgIcon.toByteArray() }

        AsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(svgData)
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
            contentDescription = trackable.name,
            modifier = Modifier.fillMaxSize().padding(4.dp),
        )
      }

      // Details
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = trackable.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
      }

      // Actions
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
            onClick = onEdit,
            modifier =
                Modifier.size(36.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
        ) {
          Icon(
              Icons.Default.Edit,
              contentDescription = "Edit",
              modifier = Modifier.size(18.dp),
              tint = MaterialTheme.colorScheme.primary,
          )
        }

        IconButton(
            onClick = onDelete,
            modifier =
                Modifier.size(36.dp)
                    .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp)),
        ) {
          Icon(
              Icons.Default.Delete,
              contentDescription = "Delete",
              modifier = Modifier.size(18.dp),
              tint = MaterialTheme.colorScheme.error,
          )
        }
      }
    }
  }
}
