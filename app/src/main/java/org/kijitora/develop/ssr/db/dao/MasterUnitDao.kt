/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.kijitora.develop.ssr.db.dataclass.entity.MasterUnit

/**
 * マスター機体のDAO.
 */
@Dao
interface MasterUnitDao {

    /**
     * マスター機体リストを挿入する.
     * Primary Keyが競合した場合は置き換える.
     *
     * @param units マスター機体リスト
     * @Insert(onConflict = OnConflictStrategy.REPLACE)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(units: List<MasterUnit>) // 初期データ投入用

    @Query("SELECT * FROM master_units WHERE unitName LIKE '%' || :query || '%'")
    fun searchUnitsByName(query: String): Flow<List<MasterUnit>>

    @Query("SELECT * FROM master_units")
    fun getAllMasterUnits(): Flow<List<MasterUnit>>

}