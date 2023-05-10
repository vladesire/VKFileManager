package com.vladesire.vkfilemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class FileManagerUIState {
    SORT_NAME_ASC,
    SORT_NAME_DESC,
    SORT_DATE_ASC,
    SORT_DATE_DESC,
    SORT_EXTENSION_ASC,
    SORT_EXTENSION_DESC,
    SORT_SIZE_ASC,
    SORT_SIZE_DESC,
}

enum class HashUiState {
    Scanning,
    Hashing,
    Done
}

data class TotalUiState (
    val fileManager: FileManagerUIState,
    val hash: HashUiState,
    val filesNumber: Int,
    val hashedFiles: Int,
    val filesChanged: Int
)

class FileManagerViewModel(
    private val repository: FilesRepository
) : ViewModel() {
    private val _uiState: MutableStateFlow<TotalUiState> =
        MutableStateFlow(
            TotalUiState(
                fileManager = FileManagerUIState.SORT_NAME_ASC,
                hash = HashUiState.Scanning,
                filesNumber = 0,
                hashedFiles = 0,
                filesChanged = 0
            )
        )

    val uiState = _uiState.asStateFlow()

    fun updateFileManagerUIState(newUIState: FileManagerUIState) {
        _uiState.update {
            it.copy(
                fileManager = newUIState
            )
        }
    }

    fun startHashingFiles() {
        viewModelScope.launch {
            val paths = repository.getAllFilesPaths()

            _uiState.update {
                it.copy(
                    hash = HashUiState.Hashing,
                    filesNumber = paths.size
                )
            }


            // TODO: DISPLAY THIS LIST!!
            val changed = mutableListOf<String>()

            paths.map { path ->

                var hash = ""
                // getHash uses blocking input stream
                withContext(Dispatchers.IO) {
                    hash = repository.getHash(path)

                    repository.getSavedHash(path)?.let {

                        if (it != hash) {
                            changed += path
                        }

                    }
                }


                repository.saveFile(path, hash)


                _uiState.update {
                    it.copy(
                        hashedFiles = it.hashedFiles + 1,
                        filesChanged = changed.size
                    )
                }

            }


        }
    }


}

class FileManagerViewModelFactory(
    private val repository: FilesRepository = FilesRepository.get()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FileManagerViewModel(repository) as T
    }
}