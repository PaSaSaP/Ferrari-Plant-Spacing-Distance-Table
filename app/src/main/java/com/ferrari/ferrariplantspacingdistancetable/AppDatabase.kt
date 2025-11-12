package com.ferrari.ferrariplantspacingdistancetable

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Database(entities = [Distanza::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun distanzaDao(): DistanzaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        var allGearsList: List<Int> = emptyList()
        private const val TAG = "DB_DEBUG"

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ferrari_database"
                ).build()
                INSTANCE = instance

                // Wypełnij bazę danymi z CSV
                CoroutineScope(Dispatchers.IO).launch {
                    instance.fillDatabase(context)
                    val allGears = instance.distanzaDao().getAllGears()
                    allGearsList = allGears
                    instance.printAllData()
                }
                instance
            }
        }
    }

    private suspend fun fillDatabase(context: Context) {
        val dao = distanzaDao()
        if (dao.find(38, 12, "old") != null) return // już wypełnione

        listOf("old" to "distances_old.csv", "new" to "distances_new.csv").forEach { (model, fileName) ->
            context.assets.open("data/$fileName").bufferedReader().use { reader ->
                reader.lineSequence()
                    .filter { it.isNotBlank() && !it.startsWith("A") }
                    .forEach { line ->
                        val parts = line.split(",").map { it.trim() }
                        if (parts.size == 3) {
                            dao.insertAll(
                                Distanza(
                                    a = parts[0].toInt(),
                                    b = parts[1].toInt(),
                                    distance = parts[2].toDouble(),
                                    model = model
                                )
                            )
                        }
                    }
            }
        }
    }

    suspend fun printAllData() {
        val dao = distanzaDao()
        val all = dao.getAllData() // dodamy tę metodę w DAO
        Log.d(TAG, "=== WSZYSTKIE DANE W BAZIE (${all.size} rekordów) ===")
//        all.forEach { d ->
//            Log.d(TAG, "ID=${d.id} | A=${d.a} | B=${d.b} | dist=${d.distance} | model=${d.model}")
//        }
//        val oldBList = dao.getAllB("old").first()
//        Log.d(TAG, "===== old B W BAZIE (${oldBList.size} rekordów) ===")
//        oldBList.forEach { b -> Log.d(TAG, "b = $b") }
//        Log.d(TAG, "=== KONIEC DANYCH old B ===")
//
//        val newBList = dao.getAllB("new").first()
//        Log.d(TAG, "===== new B W BAZIE (${newBList.size} rekordów) ===")
//        newBList.forEach { b -> Log.d(TAG, "b = $b") }
//        Log.d(TAG, "=== KONIEC DANYCH new B ===")

        Log.d(TAG, "=== KONIEC DANYCH ===")
    }
}
