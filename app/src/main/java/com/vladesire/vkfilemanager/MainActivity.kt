package com.vladesire.vkfilemanager

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.vladesire.vkfilemanager.ui.FileManagerScreen
import java.io.File
import java.nio.file.attribute.BasicFileAttributes
import java.util.*


class MyFileProvider : FileProvider(R.xml.provider_paths)


// Application is built for newer APIs: it targets Android 13, so it uses new policies towards storage usage
// Project is entirely written in Jetpack Compose
// Database is powered by Room & Kotlin Coroutines

// File system can be easily navigated
// Press folder to open it, press file to fire intent ACTION_VIEW, click SHARE to fire ACTION_SEND
// List of changed files can be displayed, but I had no time left to make it

// Application is tested on Android 13 device

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // For now restart app to apply changes
        val isExternalManager = Environment.isExternalStorageManager()

        setContent {

            if (!isExternalManager) {
                Dialog(onDismissRequest = { /*TODO*/ }) {
                    Card() {
                        Column(
                            Modifier.padding(32.dp)
                        ) {
                            Text("android.permission.MANAGE_EXTERNAL_STORAGE is a must!", Modifier.padding(8.dp))
                            Text("Please go the settings and grant All files access", Modifier.padding(8.dp))
                            Text("(not enough time to set up an intent)", Modifier.padding(8.dp))
                            Text("restart app to apply changes", Modifier.padding(8.dp))
                            Text("Tested on Android API 33", Modifier.padding(8.dp))
                        }
                    }
                }
            } else {
                FileManagerScreen()
            }
        }
    }
}