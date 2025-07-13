/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.db.dao

import androidx.room.Dao
import androidx.room.Update
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

import kotlinx.coroutines.flow.Flow
import org.kijitora.develop.ssr.db.dataclass.UserUnitWithMaster
import org.kijitora.develop.ssr.db.dataclass.entity.UserUnit

/**
 * ユーザー機体のDAO.
 */
@Dao
interface UserUnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(userUnit: UserUnit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(userUnits: List<UserUnit>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoreWhenConflict(userUnits: List<UserUnit>)

    @Update
    suspend fun update(userUnit: UserUnit)

    @Query("DELETE FROM user_units WHERE accountId = :accountId")
    suspend fun delete(accountId: Long)

    @Query("SELECT * FROM user_units WHERE accountId = :accountId")
    fun getUserUnits(accountId: Long): Flow<List<UserUnit>>

    @Query("SELECT * FROM user_units WHERE accountId = :accountId AND unitName = :unitName")
    suspend fun getUserUnit(accountId: Long, unitName: String): UserUnit?

    @Transaction
    @Query("SELECT units.* FROM user_units units INNER JOIN master_units master ON units.unitName = master.unitName WHERE units.accountId = :accountId AND master.unitName LIKE '%' || :query || '%' ORDER BY master.seq ASC")
    fun searchUserUnitsByAccountId(accountId: Long, query: String): Flow<List<UserUnitWithMaster>>

    @Transaction
    @Query("SELECT * FROM user_units WHERE accountId = :accountId")
    fun getAllUserUnits(accountId: Long): Flow<List<UserUnit>>

    @Transaction
    @Query("SELECT * FROM user_units WHERE accountId = :accountId")
    fun getAllUserUnitsWithMaster(accountId: Long): Flow<List<UserUnitWithMaster>>

}