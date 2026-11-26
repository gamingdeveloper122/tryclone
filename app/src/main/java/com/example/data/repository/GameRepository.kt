package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val dao: PuzzleNovaDao) {

    val userProgressFlow: Flow<UserProgress?> = dao.getUserProgressFlow()
    val levelProgressFlow: Flow<List<LevelProgress>> = dao.getAllLevelProgressFlow()
    val allAchievementsFlow: Flow<List<Achievement>> = dao.getAllAchievementsFlow()
    val savedGameFlow: Flow<SavedGame?> = dao.getSavedGameFlow()

    suspend fun saveUserProgress(progress: UserProgress) {
        dao.saveUserProgress(progress)
    }

    fun getMissionsFlow(period: String): Flow<List<Mission>> {
        return dao.getMissionsByPeriodFlow(period)
    }

    suspend fun initializeDatabaseIfEmpty() {
        val currentProgress = dao.getUserProgressDirect()
        if (currentProgress == null) {
            // Seed initial User Progress
            val initialProgress = UserProgress()
            dao.saveUserProgress(initialProgress)

            // Seed Missions
            val initialMissions = listOf(
                Mission("m_level_5", "Complete 5 Levels", "levels", 5, 0, "daily", 200, 10, 0),
                Mission("m_coins_100", "Earn 100 Coins", "coins", 100, 0, "daily", 100, 5, 0),
                Mission("m_no_hints_3", "Solve 3 Puzzles without Hints", "no_hints", 3, 0, "daily", 150, 8, 0),
                Mission("m_time_20", "Play 20 Minutes (1200s)", "time", 1200, 0, "weekly", 500, 25, 0),
                Mission("m_level_25", "Complete 25 Levels", "levels", 25, 0, "weekly", 600, 30, 0),
                Mission("m_spins_5", "Spin the Lucky Wheel 5 Times", "spins", 5, 0, "weekly", 300, 15, 0),
                Mission("m_level_100", "Complete 100 Levels", "levels", 100, 0, "monthly", 2000, 100, 0)
            )
            dao.saveMissions(initialMissions)

            // Seed Achievements
            val initialAchievements = listOf(
                Achievement("ach_first_win", "First Victory", "Complete your first puzzle level!", 1, 0, 100, 0),
                Achievement("ach_win_10", "Puzzle Apprentice", "Complete 10 levels", 10, 0, 250, 0),
                Achievement("ach_win_50", "Puzzle Expert", "Complete 50 levels", 50, 0, 1000, 0),
                Achievement("ach_win_100", "Puzzle Master", "Complete 100 levels", 100, 0, 2500, 0),
                Achievement("ach_use_5_hints", "Curious Mind", "Use 5 hints to solve puzzles", 5, 0, 150, 0),
                Achievement("ach_spins_3", "Lucky Roller", "Spin the Lucky Wheel 3 times", 3, 0, 200, 0),
                Achievement("ach_spend_1000", "Big Spender", "Spend 1,000 coins in the Shop", 1000, 0, 300, 0),
                Achievement("ach_perfect_5", "Perfect Strategist", "Earn a 3-star rating 5 times", 5, 0, 500, 0)
            )
            dao.saveAchievements(initialAchievements)
        }
    }

    suspend fun saveLevelCompletion(levelId: Int, stars: Int, bestScore: Int, timeTakenSeconds: Int, usedHints: Boolean) {
        val existing = dao.getLevelProgress(levelId)
        val isFirstTime = existing == null
        val newStars = if (existing != null) maxOf(existing.stars, stars) else stars
        val newScore = if (existing != null) maxOf(existing.bestScore, bestScore) else bestScore
        val newTime = if (existing != null) minOf(existing.timeTakenSeconds, timeTakenSeconds) else timeTakenSeconds

        // 1. Save level progress
        dao.saveLevelProgress(LevelProgress(levelId, newStars, newScore, newTime, System.currentTimeMillis()))

        // 2. Fetch UserProgress to update
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        val earnedCoins = 50 + stars * 25 + (if (stars == 3) 50 else 0) // Perfect bonus
        val earnedGems = if (stars == 3) 5 else 2
        val xpGain = 10 * stars

        var updatedCoins = progress.coins + earnedCoins
        var updatedGems = progress.gems + earnedGems
        var updatedXp = progress.xp + xpGain
        var updatedLevel = progress.level
        val xpNeeded = updatedLevel * 100
        if (updatedXp >= xpNeeded) {
            updatedXp -= xpNeeded
            updatedLevel++
            updatedCoins += 100 // Level up reward
            updatedGems += 10
        }

        val updatedLevelsCount = if (isFirstTime) progress.statsLevelsCompleted + 1 else progress.statsLevelsCompleted

        val newProgress = progress.copy(
            coins = updatedCoins,
            gems = updatedGems,
            xp = updatedXp,
            level = updatedLevel,
            statsLevelsCompleted = updatedLevelsCount,
            statsPlayTimeSeconds = progress.statsPlayTimeSeconds + timeTakenSeconds
        )
        dao.saveUserProgress(newProgress)

        // 3. Clear active saved game since the level is solved
        val activeSave = dao.getSavedGameDirect()
        if (activeSave != null && activeSave.levelId == levelId) {
            dao.clearSavedGame()
        }

        // 4. Update Missions & Achievements
        updateMissionProgress("levels", 1)
        updateMissionProgress("coins", earnedCoins)
        if (!usedHints) {
            updateMissionProgress("no_hints", 1)
        }
        updateMissionProgress("time", timeTakenSeconds)

        updateAchievementProgress("ach_first_win", 1)
        updateAchievementProgress("ach_win_10", 1)
        updateAchievementProgress("ach_win_50", 1)
        updateAchievementProgress("ach_win_100", 1)
        if (stars == 3) {
            updateAchievementProgress("ach_perfect_5", 1)
        }
    }

    suspend fun incrementHintsUsed() {
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        dao.saveUserProgress(progress.copy(statsTotalHintsUsed = progress.statsTotalHintsUsed + 1))
        updateAchievementProgress("ach_use_5_hints", 1)
    }

    suspend fun incrementLuckyWheelSpins() {
        updateMissionProgress("spins", 1)
        updateAchievementProgress("ach_spins_3", 1)
    }

    suspend fun spendCoins(amount: Int): Boolean {
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        if (progress.coins >= amount) {
            dao.saveUserProgress(progress.copy(coins = progress.coins - amount))
            updateAchievementProgress("ach_spend_1000", amount)
            return true
        }
        return false
    }

    suspend fun spendGems(amount: Int): Boolean {
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        if (progress.gems >= amount) {
            dao.saveUserProgress(progress.copy(gems = progress.gems - amount))
            return true
        }
        return false
    }

    suspend fun addRewards(coins: Int, gems: Int) {
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        dao.saveUserProgress(progress.copy(
            coins = progress.coins + coins,
            gems = progress.gems + gems
        ))
        updateMissionProgress("coins", coins)
    }

    suspend fun saveThemeSelection(themeName: String) {
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        dao.saveUserProgress(progress.copy(activeTheme = themeName))
    }

    suspend fun unlockTheme(themeName: String, coinCost: Int): Boolean {
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        val themesList = progress.unlockedThemes.split(",").map { it.trim() }.toMutableList()
        if (themesList.contains(themeName)) return true
        
        if (spendCoins(coinCost)) {
            themesList.add(themeName)
            dao.saveUserProgress(progress.copy(
                coins = progress.coins - coinCost, // already deducted in spendCoins! Wait, let's verify.
                // spendCoins already subtracted from coins, so we fetch again or copy from current progress!
                unlockedThemes = themesList.joinToString(",")
            ))
            // Let's reload user progress inside the caller to avoid double deduction
            return true
        }
        return false
    }

    suspend fun buyThemeDirect(themeName: String) {
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        val themesList = progress.unlockedThemes.split(",").map { it.trim() }.toMutableList()
        if (!themesList.contains(themeName)) {
            themesList.add(themeName)
            dao.saveUserProgress(progress.copy(unlockedThemes = themesList.joinToString(",")))
        }
    }

    suspend fun updateSettings(sound: Boolean, music: Boolean, vibration: Boolean, kidsMode: Boolean) {
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        dao.saveUserProgress(progress.copy(
            soundEnabled = sound,
            musicEnabled = music,
            vibrationEnabled = vibration,
            kidsModeEnabled = kidsMode
        ))
    }

    suspend fun saveAutosaveGame(levelId: Int, category: String, serializedState: String, moves: Int, hints: Int, seconds: Long) {
        val saved = SavedGame(
            levelId = levelId,
            category = category,
            serializedState = serializedState,
            movesCount = moves,
            hintsUsed = hints,
            timeSeconds = seconds,
            active = true
        )
        dao.saveSavedGame(saved)
    }

    suspend fun deleteAutosave() {
        dao.clearSavedGame()
    }

    // --- Missions Claim ---
    suspend fun claimMissionReward(missionId: String): Pair<Int, Int>? {
        val m = dao.getMissionById(missionId)
        if (m != null && m.status == 1) {
            // Claimable
            dao.saveMission(m.copy(status = 2)) // mark claimed
            addRewards(m.coinsReward, m.gemsReward)
            return Pair(m.coinsReward, m.gemsReward)
        }
        return null
    }

    // --- Achievements Claim ---
    suspend fun claimAchievementReward(achievementId: String): Int? {
        val a = dao.getAchievementById(achievementId)
        if (a != null && a.status == 1) {
            dao.saveAchievement(a.copy(status = 2))
            addRewards(a.coinsReward, 0)
            return a.coinsReward
        }
        return null
    }

    // --- Helper updates ---
    private suspend fun updateMissionProgress(type: String, delta: Int) {
        val allMissions = dao.getAllMissionsFlow().firstOrNull() ?: emptyList()
        for (m in allMissions) {
            if (m.type == type && m.status == 0) {
                val newVal = m.currentValue + delta
                val isCompleted = newVal >= m.targetValue
                dao.saveMission(m.copy(
                    currentValue = minOf(newVal, m.targetValue),
                    status = if (isCompleted) 1 else 0
                ))
            }
        }
    }

    private suspend fun updateAchievementProgress(id: String, delta: Int) {
        val a = dao.getAchievementById(id)
        if (a != null && a.status == 0) {
            val newVal = a.currentValue + delta
            val isCompleted = newVal >= a.targetValue
            dao.saveAchievement(a.copy(
                currentValue = minOf(newVal, a.targetValue),
                status = if (isCompleted) 1 else 0
            ))
        }
    }

    // --- Admin Operations ---
    suspend fun adminAddResources(coins: Int, gems: Int) {
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        dao.saveUserProgress(progress.copy(
            coins = (progress.coins + coins).coerceAtLeast(0),
            gems = (progress.gems + gems).coerceAtLeast(0)
        ))
    }

    suspend fun adminUnlockLevels(count: Int) {
        // Unlock level records
        for (l in 1..count) {
            val existing = dao.getLevelProgress(l)
            if (existing == null) {
                dao.saveLevelProgress(LevelProgress(l, 3, 300, 15, System.currentTimeMillis()))
            }
        }
        val progress = dao.getUserProgressDirect() ?: UserProgress()
        dao.saveUserProgress(progress.copy(statsLevelsCompleted = maxOf(progress.statsLevelsCompleted, count)))
    }

    suspend fun adminResetProgress() {
        dao.clearLevelProgress()
        dao.clearSavedGame()
        dao.clearMissions()
        dao.clearAchievements()
        dao.clearAllGameModeProgress()
        val initialProgress = UserProgress()
        dao.saveUserProgress(initialProgress)
        initializeDatabaseIfEmpty()
    }

    val allGameModeProgressFlow: Flow<List<GameModeProgress>> = dao.getAllGameModeProgressFlow()

    fun getGameModeProgressFlow(modeId: String): Flow<GameModeProgress?> {
        return dao.getGameModeProgressFlow(modeId)
    }

    suspend fun getGameModeProgressDirect(modeId: String): GameModeProgress? {
        return dao.getGameModeProgressDirect(modeId)
    }

    suspend fun getUserProgressDirect(): UserProgress? {
        return dao.getUserProgressDirect()
    }

    suspend fun saveGameModeProgress(progress: GameModeProgress) {
        dao.saveGameModeProgress(progress)
    }
}
