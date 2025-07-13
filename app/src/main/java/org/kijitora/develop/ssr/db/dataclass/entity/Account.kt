/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.db.dataclass.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(

    /**
     * アカウントID.
     */
    @PrimaryKey(autoGenerate = true)
    val accountId: Long = 0,

    /**
     * アカウント名.
     */
    val accountName: String
)