package com.ko.app.data.repository

import androidx.lifecycle.LiveData
import com.ko.app.data.dao.ScreenshotDao
import com.ko.app.data.entity.Screenshot
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@Suppress("TooManyFunctions")
class ScreenshotRepositoryImpl @Inject constructor(private val screenshotDao: ScreenshotDao) : com.ko.app.data.repository.ScreenshotRepository {

    override fun getAllScreenshots(): Flow<List<Screenshot>> {
        return screenshotDao.getAllScreenshots()
    }

    override fun getAllScreenshotsLiveData(): LiveData<List<Screenshot>> {
        return screenshotDao.getAllScreenshotsLiveData()
    }

    override fun getMarkedScreenshots(): Flow<List<Screenshot>> {
        return screenshotDao.getMarkedScreenshots()
    }

    override fun getMarkedScreenshotsLiveData(): LiveData<List<Screenshot>> {
        return screenshotDao.getMarkedScreenshotsLiveData()
    }

    override fun getKeptScreenshots(): Flow<List<Screenshot>> {
        return screenshotDao.getKeptScreenshots()
    }

    override fun getUnmarkedScreenshots(): Flow<List<Screenshot>> {
        return screenshotDao.getUnmarkedScreenshots()
    }

    override fun getMarkedCount(): Flow<Int> {
        return screenshotDao.getMarkedCount()
    }

    override suspend fun insert(screenshot: Screenshot): Long {
        return screenshotDao.insert(screenshot)
    }

    override suspend fun insertAll(screenshots: List<Screenshot>): List<Long> {
        return screenshotDao.insertAll(screenshots)
    }

    override suspend fun update(screenshot: Screenshot) {
        screenshotDao.update(screenshot)
    }

    override suspend fun delete(screenshot: Screenshot) {
        screenshotDao.delete(screenshot)
    }

    override suspend fun getById(id: Long): Screenshot? {
        return screenshotDao.getById(id)
    }

    override suspend fun getByFilePath(filePath: String): Screenshot? {
        return screenshotDao.getByFilePath(filePath)
    }

    override suspend fun getExpiredScreenshots(currentTime: Long): List<Screenshot> {
        return screenshotDao.getExpiredScreenshots(currentTime)
    }

    override suspend fun deleteById(id: Long) {
        screenshotDao.deleteById(id)
    }

    override suspend fun deleteByFilePath(filePath: String) {
        screenshotDao.deleteByFilePath(filePath)
    }

    override suspend fun markAsKept(id: Long) {
        screenshotDao.markAsKept(id)
    }

    override suspend fun markForDeletion(id: Long, deletionTime: Long) {
        screenshotDao.markForDeletion(id, deletionTime)
    }

    override suspend fun deleteAll() {
        screenshotDao.deleteAll()
    }

    override suspend fun getPagedScreenshots(offset: Int, limit: Int): List<Screenshot> {
        return screenshotDao.getAllScreenshotsPaged(limit, offset)
    }

    override suspend fun getPagedMarkedScreenshots(offset: Int, limit: Int): List<Screenshot> {
        return screenshotDao.getMarkedScreenshotsPaged(limit, offset)
    }

    override suspend fun getPagedKeptScreenshots(offset: Int, limit: Int): List<Screenshot> {
        return screenshotDao.getKeptScreenshotsPaged(limit, offset)
    }
}
