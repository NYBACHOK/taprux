package com.ghuba.taprux

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ghuba.taprux.core.CoreFfi
import com.ghuba.taprux.core.Effect
import com.ghuba.taprux.core.Event
import com.ghuba.taprux.core.Request
import com.ghuba.taprux.core.Requests
import com.ghuba.taprux.core.ViewModel

open class Core : androidx.lifecycle.ViewModel() {
  private var core: CoreFfi = CoreFfi()

  var view: ViewModel by mutableStateOf(ViewModel.bincodeDeserialize(core.view()))

  fun update(event: Event) {
    val effects = core.update(event.bincodeSerialize())

    val requests = Requests.bincodeDeserialize(effects)
    for (request in requests) {
      processEffect(request)
    }
  }

  private fun processEffect(request: Request) {
    when (val effect = request.effect) {
      is Effect.Render -> {
        this.view = ViewModel.bincodeDeserialize(core.view())
      }
    }
  }
}
