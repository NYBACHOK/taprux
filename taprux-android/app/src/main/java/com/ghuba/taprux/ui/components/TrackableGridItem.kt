package com.ghuba.taprux.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.ghuba.taprux.core.TrackableModel
import com.ghuba.taprux.toByteArray

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackableGridItem(
  trackable: TrackableModel,
  count: Int,
  showName: Boolean,
  isReadOnly: Boolean,
  onClick: () -> Unit,
  onDoubleClick: () -> Unit,
  onLongClick: () -> Unit,
) {
  val goalColor = getGoalColor(trackable, count)

  Surface(
    modifier =
      Modifier.aspectRatio(1f)
        .alpha(if (isReadOnly) 0.75f else 1f)
        .clip(RoundedCornerShape(12.dp))
        .border(
          width = 2.dp,
          color = goalColor ?: MaterialTheme.colorScheme.outlineVariant,
          shape = RoundedCornerShape(12.dp),
        )
        .combinedClickable(
          enabled = !isReadOnly,
          onClick = onClick,
          onDoubleClick = onDoubleClick,
          onLongClick = onLongClick,
        ),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
      ) {
        Box(
          modifier = Modifier.weight(1f).background(Color(0xFFBBDEFB)),
          contentAlignment = Alignment.Center,
        ) {
          val context = LocalContext.current
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

        if (showName) {
          Text(
            text = trackable.name,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
              Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 4.dp, horizontal = 2.dp),
          )
        }
      }

      // Count Badge
      if (count > 0) {
        Box(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .padding(4.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primary)
              .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = if (count > 99) "99+" else count.toString(),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

@Composable
fun getGoalColor(trackable: TrackableModel, count: Int): Color? {
  //  val threshold = trackable.goalThreshold ?: return null
  //  return when (trackable.goalType) {
  //    "more" -> if (count >= threshold) Color(0xFF0D8F7A) else null
  //    "less" -> if (count > threshold) Color(0xFF8B1A4A) else null
  //    else -> null
  //  }
  return null
}
