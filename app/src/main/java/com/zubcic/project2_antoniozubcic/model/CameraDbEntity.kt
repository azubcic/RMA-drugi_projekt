package com.zubcic.project2_antoniozubcic.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cameras")
data class CameraDbEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val manufactrer: String = "",
    val model: String = "",
    @ColumnInfo(name = "release_date") val releaseDate: Date?,
    val type: String = "",
    val image: String = ""
)
