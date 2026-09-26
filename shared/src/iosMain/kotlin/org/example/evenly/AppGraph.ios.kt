package org.example.evenly

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import org.example.evenly.data.sources.DATABASE_FILE_NAME
import org.example.evenly.data.sources.EvenlyDatabase
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionCompleteUnlessOpen
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUserDomainMask

private const val DATABASE_DIRECTORY_NAME: String = "Database"

fun createAppGraph(): AppGraph = AppGraph(Room.databaseBuilder<EvenlyDatabase>(name = "${databaseDirectory()}/$DATABASE_FILE_NAME"))

@OptIn(ExperimentalForeignApi::class)
private fun databaseDirectory(): String {
    val fileManager = NSFileManager.defaultManager
    val supportUrl = requireNotNull(fileManager.URLForDirectory(directory = NSApplicationSupportDirectory, inDomain = NSUserDomainMask, appropriateForURL = null, create = true, error = null))
    val directoryUrl = requireNotNull(supportUrl.URLByAppendingPathComponent(DATABASE_DIRECTORY_NAME))
    fileManager.createDirectoryAtURL(directoryUrl, withIntermediateDirectories = true, attributes = mapOf<Any?, Any?>(NSFileProtectionKey to NSFileProtectionCompleteUnlessOpen), error = null)
    directoryUrl.setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
    return requireNotNull(directoryUrl.path)
}
