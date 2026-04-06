package com.ghuba.taprux

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghuba.taprux.core.Event
import com.ghuba.taprux.ui.theme.TapruxTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    createInsets(findViewById<View>(android.R.id.content).rootView)
    setContent {
      TapruxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          View()
        }
      }
    }
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
fun View(core: Core = viewModel()) {
  val scope = rememberCoroutineScope()

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  TapruxTheme { View() }
}
