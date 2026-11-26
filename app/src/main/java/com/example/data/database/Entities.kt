package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 500,
    val gems: Int = 50,
    val energy: Int = 100,
    val level: Int = 1,
    val xp: Int = 0,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val kidsModeEnabled: Boolean = false,
    val lastSpinTime: Long = 0L,
    val lastDailyRewardTime: Long = 0L,
    val dailyRewardStreak: Int = 0,
    val activeTheme: String = "Elegant Dark",
    val statsLevelsCompleted: Int = 0,
    val statsTotalMoves: Int = 0,
    val statsTotalHintsUsed: Int = 0,
    val statsPlayTimeSeconds: Long = 0L,
    val doubleCoinsUnlocked: Boolean = false,
    val adFreeUnlocked: Boolean = false,
    val vipUnlocked: Boolean = false,
    val unlockedThemes: String = "Elegant Dark", // comma-separated list
    val unlockedPacks: String = "Classic Pack"   // comma-separated list
)

@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelId: Int, // 1 to 700+
    val stars: Int, // 0 to 3
    val bestScore: Int,
    val timeTakenSeconds: Int,
    val completedAt: Long
)

@Entity(tableName = "missions")
data class Mission(
    @PrimaryKey val missionId: String,
    val title: String,
    val type: String, // "levels", "coins", "no_hints", "time", "spins"
    val targetValue: Int,
    val currentValue: Int,
    val period: String, // "daily", "weekly", "monthly"
    val coinsReward: Int,
    val gemsReward: Int,
    val status: Int // 0 = IN_PROGRESS, 1 = COMPLETED (unclaimed), 2 = CLAIMED
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val achievementId: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int,
    val coinsReward: Int,
    val status: Int // 0 = LOCKED, 1 = UNLOCKED (unclaimed), 2 = CLAIMED
)

@Entity(tableName = "saved_game")
data class SavedGame(
    @PrimaryKey val id: Int = 1,
    val levelId: Int,
    val category: String,
    val serializedState: String, // custom serialization of game layout
    val movesCount: Int,
    val hintsUsed: Int,
    val timeSeconds: Long,
    val active: Boolean
)

@Entity(tableName = "game_mode_progress")
data class GameModeProgress(
    @PrimaryKey val modeId: String,
    val currentLevel: Int = 1,
    val starsEarned: Int = 0,
    val highestScore: Int = 0,
    val totalPlayTimeSeconds: Long = 0L,
    val completedCount: Int = 0
)
