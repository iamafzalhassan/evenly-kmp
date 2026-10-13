package org.example.evenly.app

import android.content.Context
import androidx.room.Room
import org.example.evenly.core.database.DATABASE_FILE_NAME
import org.example.evenly.core.database.EvenlyDatabase

fun createAppGraph(context: Context): AppGraph {
    val appContext = context.applicationContext
    val databasePath = appContext.getDatabasePath(DATABASE_FILE_NAME).absolutePath
    return AppGraph(Room.databaseBuilder<EvenlyDatabase>(context = appContext, name = databasePath))
}
