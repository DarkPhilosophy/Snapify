package com.ko.app.di

import android.content.Context
import com.ko.app.data.database.ScreenshotDatabase
import com.ko.app.data.dao.ScreenshotDao
import com.ko.app.data.preferences.AppPreferences
import com.ko.app.util.NotificationHelper
import com.ko.app.data.repository.ScreenshotRepository as ScreenshotRepositoryInterface
import com.ko.app.data.repository.ScreenshotRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): ScreenshotDatabase {
            return ScreenshotDatabase.getDatabase(context)
        }

        @Provides
        @Singleton
        fun provideScreenshotDao(db: ScreenshotDatabase): ScreenshotDao {
            return db.screenshotDao()
        }

        @Provides
        @Singleton
        fun providePreferences(@ApplicationContext context: Context): AppPreferences {
            return AppPreferences(context)
        }

        @Provides
        @Singleton
        fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
            return NotificationHelper(context)
        }
    }

    @Binds
    @Singleton
    abstract fun bindScreenshotRepository(impl: ScreenshotRepositoryImpl): ScreenshotRepositoryInterface
}
