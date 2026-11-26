package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProgress::class,
        LevelProgress::class,
        Mission::class,
        Achievement::class,
        SavedGame::class,
        GameModeProgress::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PuzzleNovaDatabase : RoomDatabase() {

    abstract fun puzzleNovaDao(): PuzzleNovaDao

    companion object {
        @Volatile
        private var INSTANCE: PuzzleNovaDatabase? = null

        fun getDatabase(context: Context): PuzzleNovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PuzzleNovaDatabase::class.java,
                    "puzzle_nova_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
