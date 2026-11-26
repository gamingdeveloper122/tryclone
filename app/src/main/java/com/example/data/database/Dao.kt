package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleNovaDao {

    // --- User Progress ---
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgressFlow(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressDirect(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProgress(progress: UserProgress)

    // --- Level Progress ---
    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllLevelProgressFlow(): Flow<List<LevelProgress>>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    suspend fun getLevelProgress(levelId: Int): LevelProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevelProgress(progress: LevelProgress)

    @Query("SELECT COUNT(*) FROM level_progress")
    suspend fun getCompletedLevelsCount(): Int

    @Query("SELECT SUM(stars) FROM level_progress")
    suspend fun getTotalStarsEarned(): Int

    @Query("DELETE FROM level_progress")
    suspend fun clearLevelProgress()

    // --- Missions ---
    @Query("SELECT * FROM missions ORDER BY missionId ASC")
    fun getAllMissionsFlow(): Flow<List<Mission>>

    @Query("SELECT * FROM missions WHERE period = :period ORDER BY missionId ASC")
    fun getMissionsByPeriodFlow(period: String): Flow<List<Mission>>

    @Query("SELECT * FROM missions WHERE missionId = :missionId LIMIT 1")
    suspend fun getMissionById(missionId: String): Mission?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMissions(missions: List<Mission>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMission(mission: Mission)

    @Query("DELETE FROM missions")
    suspend fun clearMissions()

    // --- Achievements ---
    @Query("SELECT * FROM achievements ORDER BY achievementId ASC")
    fun getAllAchievementsFlow(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE achievementId = :achievementId LIMIT 1")
    suspend fun getAchievementById(achievementId: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAchievements(achievements: List<Achievement>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAchievement(achievement: Achievement)

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()

    // --- Saved Game ---
    @Query("SELECT * FROM saved_game WHERE id = 1 LIMIT 1")
    fun getSavedGameFlow(): Flow<SavedGame?>

    @Query("SELECT * FROM saved_game WHERE id = 1 LIMIT 1")
    suspend fun getSavedGameDirect(): SavedGame?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSavedGame(savedGame: SavedGame)

    @Query("DELETE FROM saved_game WHERE id = 1")
    suspend fun clearSavedGame()

    // --- Game Mode Progress ---
    @Query("SELECT * FROM game_mode_progress WHERE modeId = :modeId LIMIT 1")
    fun getGameModeProgressFlow(modeId: String): Flow<GameModeProgress?>

    @Query("SELECT * FROM game_mode_progress WHERE modeId = :modeId LIMIT 1")
    suspend fun getGameModeProgressDirect(modeId: String): GameModeProgress?

    @Query("SELECT * FROM game_mode_progress")
    fun getAllGameModeProgressFlow(): Flow<List<GameModeProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameModeProgress(progress: GameModeProgress)

    @Query("DELETE FROM game_mode_progress")
    suspend fun clearAllGameModeProgress()
}
