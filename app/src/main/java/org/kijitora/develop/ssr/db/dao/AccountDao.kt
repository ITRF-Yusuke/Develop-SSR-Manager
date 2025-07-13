/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.kijitora.develop.ssr.db.dataclass.entity.Account

/**
 * アカウントのDAO.
 *
 * @Dao このインターフェイスはDAOである.
 */
@Dao
interface AccountDao {

    /**
     * アカウントを挿入する.
     * アカウント名を渡してアカウントIDを自動生成し、アカウントIDを返す.
     * Primary Keyが競合した場合は無視する.
     *
     * @param account アカウント
     * @return アカウントID
     * @Insert(onConflict = OnConflictStrategy.IGNORE) アカウントを挿入する.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(account: Account): Long

    /**
     * 全てのアカウントを取得する.
     * Primary Keyが競合した場合は無視する.
     *
     * @return アカウントリスト
     */
    @Query("SELECT * FROM accounts")
    fun getAll(): Flow<List<Account>>

    /**
     * レコードを削除する.
     * アカウントIDの一致を条件とする.
     *
     * @param アカウントID
     */
    @Query("DELETE FROM accounts WHERE accountId = :accountId")
    suspend fun delete(accountId: Long)

}

