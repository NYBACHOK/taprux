package com.ghuba.taprux

import android.util.Log
import com.ghuba.taprux.core.AppliedChanges
import com.ghuba.taprux.core.CoreFfi
import com.ghuba.taprux.core.CruxShell
import com.ghuba.taprux.core.Effect
import com.ghuba.taprux.core.Event
import com.ghuba.taprux.core.Request
import com.ghuba.taprux.core.Requests
import com.ghuba.taprux.core.ViewModel
import com.ghuba.taprux.events.TrackableAdded
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

open class Core : androidx.lifecycle.ViewModel() {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  private val coreFfi =
      CoreFfi(
          object : CruxShell {
            override fun processEffects(bytes: ByteArray) {
              scope.launch { handleEffects(bytes) }
            }
          }
      )

  private val _viewModel: MutableStateFlow<ViewModel> = MutableStateFlow(getViewModel())
  val viewModel: StateFlow<ViewModel> = _viewModel.asStateFlow()

  private val _error: MutableStateFlow<Pair<String, Boolean>?> = MutableStateFlow(null)
  val error: StateFlow<Pair<String, Boolean>?> = _error.asStateFlow()

  fun update(event: Event) {
    scope.launch {
      val effects = coreFfi.update(event.bincodeSerialize())
      handleEffects(effects)
    }
  }

  private fun handleEffects(effects: ByteArray) {
    val requests = Requests.bincodeDeserialize(effects)
    for (request in requests) {
      processRequest(request)
    }
  }

  private fun processRequest(request: Request) {
    Log.i(TAG, "processRequest: $request")

    when (request.effect) {
      is Effect.Render -> {
        render()
      }
      is Effect.Changes -> {
        when ((request.effect as Effect.Changes).value) {
          AppliedChanges.USERTRACKABLE -> EventBus.getDefault().post(TrackableAdded())
        }
      }
      is Effect.Error -> {
        val errorMessage = (request.effect as Effect.Error).value
        _error.value = errorMessage to false
      }
      else -> {}
    }
  }

  fun dismissError() {
    _error.value = null
  }

  private fun render() {
    _viewModel.value = getViewModel()
  }

  private fun getViewModel(): ViewModel {
    return ViewModel.bincodeDeserialize(coreFfi.view())
  }

  companion object {
    private const val TAG = "Core"
  }
}
