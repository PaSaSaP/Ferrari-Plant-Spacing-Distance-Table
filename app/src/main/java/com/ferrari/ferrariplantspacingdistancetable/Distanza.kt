package com.ferrari.ferrariplantspacingdistancetable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "distances")
data class Distanza(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val a: Int,
    val b: Int,
    val distance: Double,
    val model: String // "old" lub "new"
)