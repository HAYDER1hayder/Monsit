package com.eloueduniv.monsit.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.eloueduniv.monsit.data.local.dao.CallDao
import com.eloueduniv.monsit.data.local.entity.CallEntity

@Database(entities = [CallEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callDao(): CallDao
}
