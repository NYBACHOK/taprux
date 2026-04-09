package com.ghuba.taprux

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ghuba.taprux.core.Event
import com.ghuba.taprux.core.QueryRequest
import com.ghuba.taprux.ui.pages.insights.InsightsPage
import com.ghuba.taprux.ui.pages.library.LibraryPage
import com.ghuba.taprux.ui.pages.settings.SettingsScreen
import com.ghuba.taprux.ui.pages.track.TrackPage
import com.ghuba.taprux.ui.theme.TapruxTheme

enum class AppPage {
  Library,
  Track,
  Insights,
  Settings,
}

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
  val activePage = remember { mutableStateOf(AppPage.Track) }

  Column(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.weight(1f)) {
      when (activePage.value) {
        AppPage.Library -> LibraryPage()
        AppPage.Track -> TrackPage(trackables = viewState.trackables)
        AppPage.Insights -> InsightsPage()
        AppPage.Settings ->
            SettingsScreen(
                state = viewState.settings,
                onFirstRun = { core.update(Event.Query(QueryRequest.Settings)) },
                onBackClick = { activePage.value = AppPage.Track },
                onSettingsChange = { core.update(Event.Query(QueryRequest.UpdateSettings(it))) },
                onExportCsv = {},
                onCreateBackup = {},
                onRestoreBackup = {},
                onRemoveAccess = {},
                onResetEverything = {},
                onCancelAccount = {},
            )
      }
    }

    TabBar(activePage = activePage.value, onPageSelected = { activePage.value = it })
  }
}

@Composable
fun TabBar(activePage: AppPage, onPageSelected: (AppPage) -> Unit) {
  Row(
      modifier =
          Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surface),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    TabItem(
        page = AppPage.Library,
        iconRes = R.drawable.ic_library,
        isActive = activePage == AppPage.Library,
        onSelect = onPageSelected,
    )
    TabItem(
        page = AppPage.Track,
        iconRes = R.drawable.ic_track,
        isActive = activePage == AppPage.Track,
        onSelect = onPageSelected,
    )
    TabItem(
        page = AppPage.Insights,
        iconRes = R.drawable.ic_insights,
        isActive = activePage == AppPage.Insights,
        onSelect = onPageSelected,
    )
    TabItem(
        page = AppPage.Settings,
        iconRes = R.drawable.ic_settings,
        isActive = activePage == AppPage.Settings,
        onSelect = onPageSelected,
    )
  }
}

@Composable
fun TabItem(page: AppPage, iconRes: Int, isActive: Boolean, onSelect: (AppPage) -> Unit) {
  Box(
      modifier = Modifier.size(48.dp).clickable { onSelect(page) },
      contentAlignment = Alignment.Center,
  ) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(iconRes).build(),
        contentDescription = page.name,
        modifier = Modifier.size(24.dp),
    )
  }
}
