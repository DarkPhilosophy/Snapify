package com.ko.app.di

import com.ko.app.data.preferences.AppPreferences
import com.ko.app.data.repository.ScreenshotRepository
import com.ko.app.util.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReceiverEntryPoint {
    fun repository(): ScreenshotRepository
    fun preferences(): AppPreferences
    fun notificationHelper(): NotificationHelper
}
