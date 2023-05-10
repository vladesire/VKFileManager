package com.vladesire.vkfilemanager.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FilesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFile(file: FileEntity)

    @Query("SELECT * FROM files")
    suspend fun getFiles(): List<FileEntity>

    @Query("SELECT hash FROM files WHERE path = :filePath")
    suspend fun getHash(filePath: String): String?

}