/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.db.dataclass.entity

import androidx.room.Entity

/**
 * ユーザー機体.
 */
@Entity(
    // テーブル名
    tableName = "user_units",
    // プライマリキー
    primaryKeys = ["accountId", "unitName"]
)
data class UserUnit(

    /**
     * アカウントID.
     */
    val accountId: Long,

    /**
     * 機体名.
     */
    val unitName: String,

    /**
     * 限界突破.
     */
    var breakThroughCount: String = "未所持", // "未所持", "0", "1", "2", "3"

    /**
     * 研究技術書.
     */
    var technicalManualCount: Int = 0
)
