package com.vladesire.vkfilemanager.ui

import android.content.Intent
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowRow
import com.vladesire.vkfilemanager.FileInfo
import com.vladesire.vkfilemanager.FileManagerUIState
import com.vladesire.vkfilemanager.FileManagerViewModel
import com.vladesire.vkfilemanager.R
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit


fun Long.memorySize(): String {
    return if (this > 1024L*1024*1024) {
        "%.2f GB".format(this / (1024.0*1024*1024))
    } else if (this > 1024*1024) {
        "%.2f MB".format(this / (1024.0*1024))
    } else if (this > 1024) {
        "%.2f KB".format(this / 1024.0)
    } else {
        "$this Bytes"
    }
}

@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel = viewModel()
) {
    var ext = remember { Environment.getExternalStorageDirectory() }
    var root by remember { mutableStateOf(ext) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text("$root")
                },
                navigationIcon = {
                    IconButton(
                        enabled = root != ext,
                        onClick = {
                            root = root.parentFile
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back icon",
                            modifier = Modifier
                                .size(32.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        FileManagerScreen(
            folder = root,
            uiState = uiState,
            updateUIState = viewModel::updateUIState,
            goTo = { folder ->
                root = folder
            },
            modifier = Modifier.padding(paddingValues)
        )
    }

}

private data class SortButtonState(
    val name: String,
    val asc: FileManagerUIState,
    val desc: FileManagerUIState
)

@Composable
fun FileManagerScreen(
    folder: File,
    uiState: FileManagerUIState,
    updateUIState: (FileManagerUIState) -> Unit,
    goTo: (File) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current


    val sortButtons = remember {
        listOf(
            SortButtonState("Name", FileManagerUIState.SORT_NAME_ASC, FileManagerUIState.SORT_NAME_DESC),
            SortButtonState("Date", FileManagerUIState.SORT_DATE_ASC, FileManagerUIState.SORT_DATE_DESC),
            SortButtonState("Extension", FileManagerUIState.SORT_EXTENSION_ASC, FileManagerUIState.SORT_EXTENSION_DESC),
            SortButtonState("Size", FileManagerUIState.SORT_SIZE_ASC, FileManagerUIState.SORT_SIZE_DESC),
        )
    }

    LazyColumn(
        modifier = modifier
    ) {


        item {
            LazyRow(
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Text("Sort", Modifier.padding(4.dp))
                }
                items(sortButtons) { button ->
                    Button(
                        onClick = {
                            if (uiState == button.asc) {
                                updateUIState(button.desc)
                            } else {
                                updateUIState(button.asc)
                            }
                        },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(button.name)

                            if (uiState == button.asc || uiState == button.desc) {
                                val icon = if (uiState == button.asc) {
                                    R.drawable.ic_down
                                } else {
                                    R.drawable.ic_up
                                }
                                Icon(
                                    painter = painterResource(id =  icon),
                                        tint = Color.Unspecified,
                                        contentDescription = "File icon",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(4.dp)
                                    )
                            }


                        }
                    }
                }
            }
        }

        folder.listFiles()?.let { files ->

            val fileInfos = files.map { file ->
                FileInfo(
                    file,
                    Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                )
            }

            // It really works
            val sorted = when (uiState) {
                FileManagerUIState.SORT_NAME_ASC -> {
                    fileInfos.sortedBy { it.file.name }
                }
                FileManagerUIState.SORT_NAME_DESC -> {
                    fileInfos.sortedBy { it.file.name }.reversed()
                }
                FileManagerUIState.SORT_DATE_ASC -> {
                    fileInfos.sortedBy { it.attributes.creationTime() }
                }
                FileManagerUIState.SORT_DATE_DESC -> {
                    fileInfos.sortedBy { it.attributes.creationTime() }.reversed()
                }
                FileManagerUIState.SORT_EXTENSION_ASC -> {
                    fileInfos.sortedBy { it.file.extension }
                }
                FileManagerUIState.SORT_EXTENSION_DESC -> {
                    fileInfos.sortedBy { it.file.extension }.reversed()
                }
                FileManagerUIState.SORT_SIZE_ASC -> {
                    fileInfos.sortedBy { it.attributes.size() }
                }
                FileManagerUIState.SORT_SIZE_DESC -> {
                    fileInfos.sortedBy { it.attributes.size() }.reversed()
                }
            }

            items(sorted) { fileInfo ->

//                        val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)

//                        fileInfo.file.toUri()

                Card(
                    backgroundColor = Color(0xFFFFEBEE),
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .clickable {
                            if (fileInfo.file.isDirectory) {
                                goTo(fileInfo.file)
                            } else {
                                val fileUri = FileProvider.getUriForFile(
                                    context,
                                    "com.vladesire.vkfilemanager.fileprovider",
                                    fileInfo.file
                                )

                                // Without setting type almost no application can handle it
                                val type = MimeTypeMap
                                    .getSingleton()
                                    .getMimeTypeFromExtension(fileInfo.file.extension)


                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(fileUri, type)
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)


                                val chooser = Intent.createChooser(intent, "Open with")

                                context.startActivity(chooser)
                            }
                        }
                ) {
                    Column() {
                        FlowRow(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 16.dp)
                        ) {

                            val icons = mapOf(
                                "jpg" to R.drawable.ic_jpg,
                                "mp4" to R.drawable.ic_mp4,
                                "doc" to R.drawable.ic_doc,
                                "docx" to R.drawable.ic_doc,
                                "png" to R.drawable.ic_png,
                                "txt" to R.drawable.ic_txt,
                                "pdf" to R.drawable.ic_pdf
                            )

                            val icon = if (fileInfo.file.isDirectory) {
                                R.drawable.ic_folder
                            } else {
                                icons[fileInfo.file.extension] ?: R.drawable.ic_file
                            }

                            Icon(
                                painter = painterResource(id = icon),
                                tint = Color.Unspecified,
                                contentDescription = "File icon",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(4.dp)
                            )

                            val name = fileInfo.file.nameWithoutExtension.ifEmpty {
                                    fileInfo.file.name
                            }

                            Text(name, Modifier.padding(horizontal = 4.dp))

                            if (fileInfo.file.isFile) {
                                fileInfo.attributes.size().let {
                                    Text(it.memorySize(), Modifier.padding(horizontal = 4.dp))
                                }
                            }


                            fileInfo.attributes.creationTime().let {
                                Text(SimpleDateFormat("MM/dd/yyyy").format(it.toMillis()), Modifier.padding(horizontal = 4.dp))
                            }


                        }

                        if (fileInfo.file.isFile) {
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

                                context.startActivity(Intent.createChooser(intent, "Send with"))

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