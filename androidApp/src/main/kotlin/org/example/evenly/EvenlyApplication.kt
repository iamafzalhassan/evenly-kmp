package org.example.evenly

import android.app.Application

class EvenlyApplication : Application() {
    val graph: AppGraph by lazy { createAppGraph(this) }
}
