package com.ghuba.taprux

import android.app.Application

class Taprux : Application() {
  val core: Core by lazy { Core() }
}
