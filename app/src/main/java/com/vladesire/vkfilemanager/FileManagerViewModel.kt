package com.vladesire.vkfilemanager

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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


class FileManagerViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<FileManagerUIState> =
        MutableStateFlow(FileManagerUIState.SORT_NAME_ASC)

    val uiState = _uiState.asStateFlow()

    fun updateUIState(newUIState: FileManagerUIState) {
        _uiState.value = newUIState
    }
}