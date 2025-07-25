package com.example.chamabuddy.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "groups")
data class Group(
    @PrimaryKey
    @ColumnInfo(name = "group_id")
    val groupId: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "admin_id")
    val adminId: String,

    @ColumnInfo(name = "admin_name")
    val adminName: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "total_savings")
    val totalSavings: Double = 0.0
)