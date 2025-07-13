/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.kijitora.develop.ssr.db.dataclass.entity.Account
import org.kijitora.develop.ssr.db.dao.AccountDao
import org.kijitora.develop.ssr.db.dataclass.entity.MasterUnit
import org.kijitora.develop.ssr.db.dao.MasterUnitDao
import org.kijitora.develop.ssr.db.dataclass.entity.UserUnit
import org.kijitora.develop.ssr.db.dao.UserUnitDao

/**
 *
 */
@Database(entities = [Account::class, MasterUnit::class, UserUnit::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun masterUnitDao(): MasterUnitDao
    abstract fun userUnitDao(): UserUnitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ggene_manager_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}