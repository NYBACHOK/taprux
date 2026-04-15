package com.ghuba.taprux

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ghuba.taprux.core.Event
import com.ghuba.taprux.core.QueryRequest
import com.ghuba.taprux.events.TrackableAdded
import com.ghuba.taprux.ui.components.TabItem
import com.ghuba.taprux.ui.pages.edit.EditPage
import com.ghuba.taprux.ui.pages.insights.InsightsPage
import com.ghuba.taprux.ui.pages.settings.SettingsScreen
import com.ghuba.taprux.ui.pages.track.TrackPage
import com.ghuba.taprux.ui.theme.TapruxTheme
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

enum class AppPage {
  Edit,
  Track,
  Insights,
  Settings,
}

class MainActivity : ComponentActivity() {
  private val core: Core by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    createInsets(findViewById<View>(android.R.id.content).rootView)

    setContent { TapruxTheme(dynamicColor = false) { View(core) } }

    if (savedInstanceState == null) {
      // Only runs on first creation, not on color mode changes or anything else
      core.update(Event.Initialize)
    }
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  @Suppress("unused")
  internal fun onAddTrackableEvent(event: TrackableAdded) {
    Toast.makeText(this, "Added trackable to list", Toast.LENGTH_SHORT).show()
  }
}

fun createInsets(view: View?) {
  if (view == null) return

  ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
    val types = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
    val insets = windowInsets.getInsets(types)

    v.setPadding(0, insets.top, 0, insets.bottom)

    WindowInsetsCompat.CONSUMED
  }
}

@Composable
fun View(core: Core) {
  val viewState by core.viewModel.collectAsState()
  val activePage = remember { mutableStateOf(AppPage.Track) }

  Column(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.weight(1f)) {
      when (activePage.value) {
        AppPage.Edit ->
            EditPage(
                trackables = viewState.allTrackables,
                userTrackables = viewState.userTrackables,
                onAddUserTrackable = {
                  core.update(Event.Query(QueryRequest.AddUserTrackable(it.toUInt())))
                },
                showNames = viewState.settings.showTrackableNames,
            )
        AppPage.Track ->
            TrackPage(
                trackables = viewState.userTrackables,
                todayCounts =
                    viewState.occurrences.map { it.key.toInt() to it.value.toInt() }.toMap(),
                showNames = viewState.settings.showTrackableNames,
                onIncrement = { core.update(Event.Query(QueryRequest.AddOccurrence(it.toUInt()))) },
                onDecrement = {
                  core.update(Event.Query(QueryRequest.DeleteOccurrence(it.toUInt())))
                },
                onNavigateToDetails = {
                  core.update(Event.Query(QueryRequest.Details(it.toUInt())))
                },
            )
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
  NavigationBar(
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.onSurface,
      tonalElevation = 8.dp,
  ) {
    TabItem(
        page = AppPage.Edit,
        icon = Icons.Default.Add,
        isActive = activePage == AppPage.Edit,
        onSelect = onPageSelected,
    )
    TabItem(
        page = AppPage.Track,
        icon = Icons.Default.CheckCircle,
        isActive = activePage == AppPage.Track,
        onSelect = onPageSelected,
    )
    TabItem(
        page = AppPage.Insights,
        icon = Icons.Default.BarChart,
        isActive = activePage == AppPage.Insights,
        onSelect = onPageSelected,
    )
    TabItem(
        page = AppPage.Settings,
        icon = Icons.Default.Settings,
        isActive = activePage == AppPage.Settings,
        onSelect = onPageSelected,
    )
  }
}
