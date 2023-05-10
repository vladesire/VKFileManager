package com.vladesire.vkfilemanager

import android.app.Application

class FileManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FilesRepository.initialize(this)
    }
}