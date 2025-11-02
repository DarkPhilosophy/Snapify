package com.ko.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ko.app.data.entity.Screenshot
import com.ko.app.data.repository.ScreenshotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: ScreenshotRepository) : ViewModel() {

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab

    // Expose screenshots as a StateFlow that UI can observe
    private val _screenshots = MutableStateFlow<List<Screenshot>>(emptyList())
    val screenshots: StateFlow<List<Screenshot>> = _screenshots

    private var screenshotsJob: kotlinx.coroutines.Job? = null

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
        observeScreenshots()
    }

    private fun observeScreenshots() {
        screenshotsJob?.cancel()
        screenshotsJob = viewModelScope.launch {
            val flow: Flow<List<Screenshot>> = when (_currentTab.value) {
                0 -> repository.getMarkedScreenshots()
                1 -> repository.getKeptScreenshots()
                else -> repository.getAllScreenshots()
            }

            flow.catch { /* optionally log errors */ }
                .collectLatest { list ->
                    _screenshots.value = list
                }
        }
    }

    fun markAsKept(screenshotId: Long) {
        viewModelScope.launch {
            repository.markAsKept(screenshotId)
        }
    }

    fun deleteScreenshot(screenshotId: Long, filePath: String) {
        viewModelScope.launch {
            val file = java.io.File(filePath)
            if (file.exists()) {
                file.delete()
            }
            repository.deleteById(screenshotId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        screenshotsJob?.cancel()
    }
}
