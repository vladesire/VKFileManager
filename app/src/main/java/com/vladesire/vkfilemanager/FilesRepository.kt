package com.vladesire.vkfilemanager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.os.bundleOf
import androidx.room.Room
import com.vladesire.vkfilemanager.database.FileEntity
import com.vladesire.vkfilemanager.database.FilesDatabase
import com.vladesire.vkfilemanager.ui.toHex
import java.io.File
import java.security.MessageDigest

private const val DATABASE_NAME = "files-database"

class FilesRepository private constructor(
    private val context: Context
) {
    private val database: FilesDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            FilesDatabase::class.java,
            DATABASE_NAME
        )
        .build()

    // Limits are used only for demonstration
    fun getAllFilesPaths(limit: Int? = null, offset: Int = 0): List<String> {
        val files = mutableListOf<String>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA
        )

        val selection = bundleOf(
            ContentResolver.QUERY_ARG_OFFSET to offset,

            // Sort them so that limited queries will return predictable data
            ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME),
            ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_ASCENDING,
        )

        limit?.let {
            selection.putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
        }

        val query = context.contentResolver.query(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
            projection,
            selection,
            null
        )

        query?.use { cursor ->

            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (cursor.moveToNext()) {
                files += cursor.getString(pathColumn)
            }
        }

        return files
    }

    suspend fun getSavedHash(path: String) = database.filesDao().getHash(path)

    suspend fun saveFile(path: String, hash: String) = database.filesDao().addFile(FileEntity(path, hash))

    suspend fun getSavedFiles() = database.filesDao().getFiles()

    fun getHash(path: String): String {
        val uri = Uri.fromFile(File(path))

        return try {

            val fin = context.contentResolver.openInputStream(uri)

            val buff = ByteArray(2048)


            fin?.let {
                val digest = MessageDigest.getInstance("MD5")

                var byteRead = 0

                while (byteRead != -1) {
                    byteRead = it.read(buff)

                    // TODO: FIX THIS UGLY CODE?
                    if (byteRead != -1) {
                        digest.update(buff, 0, byteRead)
                    }
                }


                it.close()

                digest.digest().toHex()
            } ?: ""

        } catch (_: Exception) {
            ""
        }

    }

    companion object {
        private var INSTANCE: FilesRepository? = null

        // Application's context lives long enough
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = FilesRepository(context)
            }
        }

        fun get(): FilesRepository {
            return INSTANCE ?: throw IllegalStateException("FilesRepository must be initialized")
        }
    }
}