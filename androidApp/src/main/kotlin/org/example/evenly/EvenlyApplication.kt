package org.example.evenly

import android.app.Application
import org.example.evenly.app.AppGraph
import org.example.evenly.app.createAppGraph

class EvenlyApplication : Application() {
    val graph: AppGraph by lazy { createAppGraph(this) }
}
