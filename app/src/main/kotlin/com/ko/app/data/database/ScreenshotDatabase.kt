package com.ko.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ko.app.data.dao.ScreenshotDao
import com.ko.app.data.entity.Screenshot

@Database(
    entities = [Screenshot::class],
    version = 3,
    exportSchema = false
)
abstract class ScreenshotDatabase : RoomDatabase() {

    abstract fun screenshotDao(): ScreenshotDao

    companion object {
        @Volatile
        private var INSTANCE: ScreenshotDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add contentUri column, nullable
                db.execSQL("ALTER TABLE screenshots ADD COLUMN contentUri TEXT")
            }
        }

        fun getDatabase(context: Context): ScreenshotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScreenshotDatabase::class.java,
                    "screenshot_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

