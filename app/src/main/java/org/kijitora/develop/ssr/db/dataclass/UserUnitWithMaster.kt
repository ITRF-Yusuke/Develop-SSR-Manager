/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.db.dataclass

import androidx.room.Embedded
import androidx.room.Relation
import org.kijitora.develop.ssr.db.dataclass.entity.MasterUnit
import org.kijitora.develop.ssr.db.dataclass.entity.UserUnit

/**
 * UserUnitとMasterUnitを結合して扱うためのデータクラス.
 */
data class UserUnitWithMaster(

    /**
     * ユーザー機体.
     */
    @Embedded val userUnit: UserUnit,

    /**
     * マスター機体.
     */
    @Relation(
        parentColumn = "unitName",
        entityColumn = "unitName"
    )
    val masterUnit: MasterUnit
)