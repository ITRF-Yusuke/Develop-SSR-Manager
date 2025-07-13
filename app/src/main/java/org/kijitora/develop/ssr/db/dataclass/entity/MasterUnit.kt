/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.db.dataclass.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * マスター機体.
 */
@Entity(tableName = "master_units")
data class MasterUnit(

    /**
     * 通番.
     */
    val seq: Int = 0,

    /**
     * シリーズ名.
     */
    val seriesName: String,

    /**
     * ユニット名.
     */
    @PrimaryKey
    val unitName: String,

    /**
     * 入手経路.
     */
    val acquisitionMethod: String,

    /**
     * 初回クリア報酬.
     */
    @ColumnInfo(defaultValue = "-")
    val firstClearReward: String,
)