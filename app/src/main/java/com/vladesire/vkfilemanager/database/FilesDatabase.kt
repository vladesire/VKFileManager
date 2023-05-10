package com.vladesire.vkfilemanager.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FileEntity::class
    ],
    version = 1
)
abstract class FilesDatabase : RoomDatabase() {
    abstract fun filesDao(): FilesDao
}