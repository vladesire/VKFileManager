package com.vladesire.vkfilemanager


import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

data class FileName(
    val id: Long,
    val name: String
)

data class FileInfo(
    val file: File,
    val attributes: BasicFileAttributes,
)


class MyFileProvider : FileProvider(R.xml.provider_paths)


class MainActivity : ComponentActivity() {

    // TODO: IT IS LIST ALL FEATURE!!!
//    fun getFileNames(volumeName: String): List<FileName> {
//
//        val files = mutableListOf<FileName>()
//
//        val projection = arrayOf(
//            MediaStore.Files.FileColumns.DOCUMENT_ID,
//            MediaStore.Files.FileColumns.DISPLAY_NAME,
//        )
//
//        val selection = bundleOf(
//            ContentResolver.QUERY_ARG_LIMIT to 1000,
//            ContentResolver.QUERY_ARG_OFFSET to 0,
//            ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME),
//            ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
//        )
//
//
//
//
//        val query = applicationContext.contentResolver.query(
//            MediaStore.Files.getContentUri(volumeName),
//            projection,
//            selection,
//            null
//        )
//
//
//
//        var counter = 0
//
//        query?.use { cursor ->
//
//            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DOCUMENT_ID)
//            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
//
//            while (cursor.moveToNext()) {
//
//                val id = cursor.getLong(idColumn)
//                val name = cursor.getString(nameColumn)
//
////                val contentUri: Uri = ContentUris.withAppendedId(
////                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
////                    id
////                )
//
//                files += FileName(id, name)
////                photos += Photo(id = counter, uri = contentUri)
//                counter += 1
//            }
//        }
//
//        return files
//    }

    // TODO: FIX IT
    @OptIn(ExperimentalPermissionsApi::class)
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val sm = getSystemService(Context.STORAGE_SERVICE) as StorageManager

        val volume = sm.primaryStorageVolume

        val name = volume.mediaStoreVolumeName

//        val filenames = getFileNames(name!!)

        // TODO: DELETE PERMISSIONS
        val ext = Environment.getExternalStorageDirectory()



        setContent {

            val permission = rememberPermissionState(permission = Manifest.permission.MANAGE_EXTERNAL_STORAGE)

            val context = LocalContext.current


            var root by remember { mutableStateOf(ext) }

//            LazyColumn() {
//                items(filenames) {
//                    Text("[${it.id}] ${it.name}", Modifier.padding(8.dp))
//                }
//            }

            var sortType by remember { mutableStateOf(1) }

            LazyColumn (
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                item {
                    if (!permission.status.isGranted) {

                        Button(onClick = { permission.launchPermissionRequest() }) {
                            Text("Request Permission")
                        }

//                LaunchedEffect(key1 = null) {
//
//                }
                    }
                }

                item {
                    Text("volume: $volume")
                    Text("volume name: $name")
                }

                item {
                    Text("$root")
                }


                if (root != ext) {
                    item {
                        Button(onClick = { root = root.parentFile }) {
                            Text("BACK")
                        }
                    }
                }


                val sortTypes = listOf(1, 2, 3, 4, 5, 6)

                items(sortTypes) { type ->
                    Row() {
                        Button(onClick = { sortType = type }) {
                            Text("$type")
                        }
                    }
                }

                root.listFiles()?.let { files ->

                    val fileInfos = files.map { file ->
                        FileInfo(
                            file,
                            Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                        )
                    }


                    // It really works
                    val sorted = when (sortType) {
                        2-> {
                            fileInfos.sortedBy { it.file.name }.reversed()
                        }
                        3 -> {
                            fileInfos.sortedBy { it.attributes.creationTime() }
                        }
                        4 -> {
                            fileInfos.sortedBy { it.attributes.creationTime() }.reversed()
                        }
                        5 -> {
                            fileInfos.sortedBy { it.file.extension }
                        }
                        6 -> {
                            fileInfos.sortedBy { it.file.extension }.reversed()
                        }

                        // 1 and default
                        else -> {
                            fileInfos.sortedBy { it.file.name }
                        }
                    }
                    items(sorted) { fileInfo ->

//                        val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)

//                        fileInfo.file.toUri()

                        // TODO: BUG; I SHOULD FALSE IT ON REDRAW (NEW LIST)
                        // TODO: I SHOULD ZERO THE WHOLE LAZYCOLUMN!!
                        var expanded by remember {
                            mutableStateOf(false)
                        }

                        Card(
                            backgroundColor = Color(0xFFFFEBEE),
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .clickable {
                                    if (fileInfo.file.isDirectory) {
                                        root = fileInfo.file
                                    } else {
                                        expanded = !expanded
                                    }
                                }
                        ) {
                            Column() {
                                Text("${fileInfo.attributes.size()}; ${fileInfo.attributes.creationTime()}; ${fileInfo.attributes.lastModifiedTime()}")
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 16.dp)
                                ) {

                                    val icons = mapOf("jpg" to R.drawable.ic_add, "mp4" to R.drawable.metrics)

                                    val icon = if (fileInfo.file.isDirectory) {
                                        R.drawable.dumbbell
                                    } else {
                                        icons[fileInfo.file.extension] ?: R.drawable.pen
                                    }

                                    Icon(painter = painterResource(id = icon), contentDescription = "File icon",
                                        Modifier
                                            .size(24.dp)
                                            .padding(4.dp))

                                    if (fileInfo.file.isDirectory) {
                                        Text("DIR: ${fileInfo.file.name}")

                                    } else {
                                        Text("${fileInfo.file.extension}: ${fileInfo.file.name}")

                                    }
                                }
                                if (expanded) {
//                                    val size = if (file.length() / (1024*1024*1024) > 0) {
//                                        "${file.length() / (1024.0*1024*1024)} GB"
//                                    } else if (file.length() / (1024*1024) > 0) {
//                                        "${file.length()/ (1024.0*1024)} MB"
//                                    } else if (file.length() / 1024 > 0) {
//                                        "${file.length() / 1024.0} KB"
//                                    } else {
//                                        "${file.length()} Bytes"
//                                    }
                                    Text("SIZE = ${fileInfo.attributes.size()}")

                                    Row() {
                                        Button(
                                            onClick = {
                                                val fileUri = FileProvider.getUriForFile(context, "com.vladesire.vkfilemanager.fileprovider", fileInfo.file)

                                                // Without setting type almost no application can handle it
                                                val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileInfo.file.extension)


                                                val intent = Intent(Intent.ACTION_VIEW)
                                                intent.setDataAndType(fileUri, type)
                                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)


                                                val chooser = Intent.createChooser(intent, "Open with")

                                                context.startActivity(chooser)
                                            }
                                        ) {
                                            Text("OPEN")
                                        }
                                        Button(onClick = {
                                            val fileUri = FileProvider.getUriForFile(context, "com.vladesire.vkfilemanager.fileprovider", fileInfo.file)
                                            val fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileInfo.file.extension)

                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = fileType
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                putExtra(
                                                    Intent.EXTRA_SUBJECT,
                                                    "Hello from the coolest file manager"
                                                )
                                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                            }

                                            startActivity(Intent.createChooser(intent, "Send with"))

                                        }) {
                                            Text("SHARE")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}