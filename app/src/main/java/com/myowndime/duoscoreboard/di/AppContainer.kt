package com.myowndime.duoscoreboard.di

import android.content.Context
import androidx.room.Room
import com.myowndime.duoscoreboard.data.local.AppDatabase
import com.myowndime.duoscoreboard.data.repository.DataStoreSettingsRepository
import com.myowndime.duoscoreboard.data.repository.RoomMatchRepository
import com.myowndime.duoscoreboard.domain.repository.MatchRepository
import com.myowndime.duoscoreboard.domain.repository.SettingsRepository

/**
 * Hand-rolled dependency container. No DI framework is used so the app keeps a
 * minimal dependency surface - every third-party library would have to be audited
 * for network or tracking code.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    val matchRepository: MatchRepository by lazy { RoomMatchRepository(database.matchRecordDao()) }

    val settingsRepository: SettingsRepository by lazy { DataStoreSettingsRepository(appContext) }
}
