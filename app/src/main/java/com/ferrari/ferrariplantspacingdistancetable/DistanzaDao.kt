package com.ferrari.ferrariplantspacingdistancetable

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DistanzaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg distanza: Distanza)

    @Query("SELECT * FROM distances WHERE model = :model AND a = :a AND b = :b LIMIT 1")
    suspend fun find(a: Int, b: Int, model: String): Distanza?

    @Query("SELECT DISTINCT a FROM distances WHERE model = :model ORDER BY a")
    fun getAllA(model: String): Flow<List<Int>>

    @Query("SELECT DISTINCT b FROM distances WHERE model = :model ORDER BY b")
    fun getAllB(model: String): Flow<List<Int>>

    @Query("SELECT DISTINCT a FROM distances UNION SELECT DISTINCT b FROM distances ORDER BY a")
    suspend fun getAllGears(): List<Int>

    @Query("SELECT * FROM distances")
    suspend fun getAllData(): List<Distanza>
}