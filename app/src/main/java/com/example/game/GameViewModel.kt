package com.example.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PuzzleNovaDatabase.getDatabase(application)
    val repository = GameRepository(database.puzzleNovaDao())

    // --- Database Bound Flows ---
    val userProgress: StateFlow<UserProgress?> = repository.userProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val levelProgressList: StateFlow<List<LevelProgress>> = repository.levelProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<Achievement>> = repository.allAchievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedGame: StateFlow<SavedGame?> = repository.savedGameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _dailyMissions = MutableStateFlow<List<Mission>>(emptyList())
    val dailyMissions = _dailyMissions.asStateFlow()

    private val _weeklyMissions = MutableStateFlow<List<Mission>>(emptyList())
    val weeklyMissions = _weeklyMissions.asStateFlow()

    private val _monthlyMissions = MutableStateFlow<List<Mission>>(emptyList())
    val monthlyMissions = _monthlyMissions.asStateFlow()

    // --- Active Gameplay State ---
    var activeLevel: PuzzleLevel? by mutableStateOf(null)
        private set
    var movesCount by mutableStateOf(0)
        private set
    var timeElapsedSeconds by mutableStateOf(0L)
        private set
    var isLevelCompleted by mutableStateOf(false)
        private set
    var activeStarsEarned by mutableStateOf(0)
        private set
    var bonusCoinsEarned by mutableStateOf(0)
        private set
    var bonusGemsEarned by mutableStateOf(0)
        private set
    var usedHintsCount by mutableStateOf(0)
        private set
    var isHintActive by mutableStateOf(false)
        private set

    // --- New Game Modes State ---
    var activeGameMode by mutableStateOf<String?>(null)
    var activeGameModeProgress by mutableStateOf<GameModeProgress?>(null)
    var survivalLives by mutableStateOf(3)

    val allGameModeProgress: StateFlow<List<GameModeProgress>> = repository.allGameModeProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadModeGameplay(modeId: String) {
        viewModelScope.launch {
            activeGameMode = modeId
            val progress = repository.getGameModeProgressDirect(modeId) ?: GameModeProgress(modeId = modeId, currentLevel = 1)
            activeGameModeProgress = progress
            
            val baseLevel = LevelGenerator.generateLevelForMode(modeId, progress.currentLevel)
            
            selectedTubeIndex = -1
            flippedCardIndices.clear()
            memoryCheckingMatches = false
            foundDifferences.clear()
            undoStackTubes.clear()
            undoStackPipes.clear()
            undoStackSlide.clear()
            isHintActive = false
            isLevelCompleted = false
            
            activeLevel = baseLevel
            movesCount = 0
            timeElapsedSeconds = if (modeId == "time_attack") 45L else 0L
            usedHintsCount = 0
            
            if (modeId == "survival") {
                survivalLives = 3
            }
            
            startTimer()
        }
    }

    // Active Selection State for Specific Games
    var selectedTubeIndex by mutableStateOf(-1) // Water Sort
    var flippedCardIndices = mutableListOf<Int>() // Memory Match
    var memoryCheckingMatches by mutableStateOf(false)
    var foundDifferences = mutableStateListOf<Int>() // Spot Difference

    // Undo Stacks (Stores snapshot of configurations)
    private val undoStackTubes = Stack<List<List<Int>>>()
    private val undoStackPipes = Stack<List<List<Pipe>>>()
    private val undoStackSlide = Stack<List<Int>>()

    // Timer Job
    private var timerJob: Job? = null

    // --- Lucky Spin Wheel State ---
    var isSpinning by mutableStateOf(false)
        private set
    var spinRotationAngle by mutableStateOf(0f)
        private set
    var spinResultRewardText by mutableStateOf("")

    // --- UI Event Alerts (Dialogs) ---
    var rewardAlertCoins by mutableStateOf(0)
    var rewardAlertGems by mutableStateOf(0)
    var rewardAlertTitle by mutableStateOf("")
    var showRewardAlert by mutableStateOf(false)

    // Simulated Ads Display State
    var showSimulatedInterstitialAd by mutableStateOf(false)
    var showSimulatedRewardedAd by mutableStateOf(false)
    private var onRewardedAdCompleted: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
            
            // Collect Missions live
            launch {
                repository.getMissionsFlow("daily").collect { _dailyMissions.value = it }
            }
            launch {
                repository.getMissionsFlow("weekly").collect { _weeklyMissions.value = it }
            }
            launch {
                repository.getMissionsFlow("monthly").collect { _monthlyMissions.value = it }
            }
        }
    }

    // --- Navigation / Game Entry ---

    fun loadLevel(levelId: Int, continueFromSave: Boolean = false) {
        viewModelScope.launch {
            val baseLevel = LevelGenerator.generateLevel(levelId)
            var restoredLevel = baseLevel
            var startMoves = 0
            var startSeconds = 0L
            var startHints = 0

            // Reset game state variables
            selectedTubeIndex = -1
            flippedCardIndices.clear()
            memoryCheckingMatches = false
            foundDifferences.clear()
            undoStackTubes.clear()
            undoStackPipes.clear()
            undoStackSlide.clear()
            isHintActive = false
            isLevelCompleted = false

            if (continueFromSave) {
                val save = repository.savedGameFlow.firstOrNull() ?: daoSavedGameDirect()
                if (save != null && save.levelId == levelId && save.active) {
                    startMoves = save.movesCount
                    startSeconds = save.timeSeconds
                    startHints = save.hintsUsed
                    
                    // Restore level state
                    restoredLevel = restoreLevelStateFromSerialized(baseLevel, save.serializedState)
                }
            }

            activeLevel = restoredLevel
            movesCount = startMoves
            timeElapsedSeconds = startSeconds
            usedHintsCount = startHints
            
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (activeGameMode == "time_attack") {
                    if (timeElapsedSeconds > 0) {
                        timeElapsedSeconds--
                        if (timeElapsedSeconds == 0L) {
                            triggerSurvivalOrTimeAttackLoss()
                        }
                    } else {
                        triggerSurvivalOrTimeAttackLoss()
                    }
                } else {
                    timeElapsedSeconds++
                }
                
                // Save occasionally for auto-save (e.g. every 10 seconds)
                if (activeGameMode == null && timeElapsedSeconds % 10 == 0L) {
                    triggerAutosave()
                }
            }
        }
    }

    private suspend fun daoSavedGameDirect(): SavedGame? {
        return database.puzzleNovaDao().getSavedGameDirect()
    }

    fun stopGameplay() {
        timerJob?.cancel()
        timerJob = null
    }

    // --- GAMEPLAY MECHANICS ---

    // 1. Water Sort Pour
    fun selectWaterSortTube(index: Int) {
        val lvl = activeLevel ?: return
        val sortState = lvl.waterSort ?: return
        val tubes = sortState.tubes

        if (selectedTubeIndex == -1) {
            // First selection: Must not be empty
            if (tubes[index].isNotEmpty()) {
                selectedTubeIndex = index
            }
        } else {
            val sourceIdx = selectedTubeIndex
            selectedTubeIndex = -1 // reset selection
            
            if (sourceIdx == index) return // tapped same tube, deselect

            val sourceTube = tubes[sourceIdx]
            val targetTube = tubes[index]

            // Validate pour
            if (targetTube.size >= sortState.capacity) return // no space
            if (sourceTube.isEmpty()) return

            val topColor = sourceTube.last()
            val targetEmpty = targetTube.isEmpty()
            val targetTopColor = if (targetEmpty) -1 else targetTube.last()

            if (targetEmpty || topColor == targetTopColor) {
                // Save to undo stack before modify
                undoStackTubes.push(cloneTubes(tubes))

                // Perform pour
                val newTubes = tubes.map { ArrayList(it) }
                val src = newTubes[sourceIdx]
                val dest = newTubes[index]

                // Pour all contiguous top segments of the same color that fit
                val colorToPour = src.last()
                while (src.isNotEmpty() && src.last() == colorToPour && dest.size < sortState.capacity) {
                    dest.add(src.removeAt(src.size - 1))
                }

                // Update state
                val newState = sortState.copy(tubes = newTubes)
                activeLevel = lvl.copy(waterSort = newState)
                movesCount++

                // Check win condition
                if (checkWaterSortWin(newTubes, sortState.capacity)) {
                    triggerLevelWin()
                } else {
                    triggerAutosave()
                }
            }
        }
    }

    private fun cloneTubes(tubes: List<List<Int>>): List<List<Int>> {
        return tubes.map { ArrayList(it) }
    }

    private fun checkWaterSortWin(tubes: List<List<Int>>, capacity: Int): Boolean {
        for (tube in tubes) {
            if (tube.isEmpty()) continue
            if (tube.size != capacity) return false
            val first = tube.first()
            if (tube.any { it != first }) return false
        }
        return true
    }

    // 2. Pipe Connect Rotation
    fun rotatePipe(row: Int, col: Int) {
        val lvl = activeLevel ?: return
        val pipeState = lvl.pipeConnect ?: return
        val grid = pipeState.grid

        // Push current grid to undo stack
        undoStackPipes.push(clonePipes(grid))

        val newGrid = grid.mapIndexed { rIndex, r ->
            r.mapIndexed { cIndex, p ->
                if (rIndex == row && cIndex == col) {
                    p.copy(rotationAngle = (p.rotationAngle + 90) % 360)
                } else {
                    p
                }
            }
        }

        val newState = pipeState.copy(grid = newGrid)
        activeLevel = lvl.copy(pipeConnect = newState)
        movesCount++

        // Check path connection flow
        if (checkPipeConnectWin(newState)) {
            triggerLevelWin()
        } else {
            triggerAutosave()
        }
    }

    private fun clonePipes(grid: List<List<Pipe>>): List<List<Pipe>> {
        return grid.map { row -> row.map { it.copy() } }
    }

    private fun checkPipeConnectWin(state: PipeConnectState): Boolean {
        // Simple graph connectivity logic: BFS/DFS from source to sink
        val visited = Array(state.size) { BooleanArray(state.size) }
        val queue: Queue<Pair<Int, Int>> = LinkedList()
        
        queue.add(Pair(state.sourceX, state.sourceY))
        visited[state.sourceY][state.sourceX] = true

        while (queue.isNotEmpty()) {
            val curr = queue.poll() ?: break
            val cx = curr.first
            val cy = curr.second

            if (cx == state.sinkX && cy == state.sinkY) {
                return true
            }

            // Neighbors: Up, Right, Down, Left
            val directions = listOf(
                Pair(0, -1), // Up
                Pair(1, 0),  // Right
                Pair(0, 1),  // Down
                Pair(-1, 0)  // Left
            )

            for (i in directions.indices) {
                val dir = directions[i]
                val nx = cx + dir.first
                val ny = cy + dir.second

                if (nx in 0 until state.size && ny in 0 until state.size && !visited[ny][nx]) {
                    // Check if current pipe connects to neighbor, AND neighbor connects to current
                    if (canConnect(state.grid[cy][cx], state.grid[ny][nx], i)) {
                        visited[ny][nx] = true
                        queue.add(Pair(nx, ny))
                    }
                }
            }
        }
        return false
    }

    // Direction indices: 0 = UP, 1 = RIGHT, 2 = DOWN, 3 = LEFT
    private fun canConnect(p1: Pipe, p2: Pipe, direction: Int): Boolean {
        if (p1.type == PipeType.BLANK || p2.type == PipeType.BLANK) return false

        // Check connection openings for p1 at "direction"
        val p1Openings = getPipeOpenings(p1)
        val p2Openings = getPipeOpenings(p2)

        val oppositeDir = (direction + 2) % 4

        return p1Openings[direction] && p2Openings[oppositeDir]
    }

    private fun getPipeOpenings(p: Pipe): BooleanArray {
        // Openings default [UP, RIGHT, DOWN, LEFT]
        val base = when (p.type) {
            PipeType.I -> booleanArrayOf(true, false, true, false) // Straight vertical
            PipeType.L -> booleanArrayOf(true, true, false, false) // Corner top-right
            PipeType.T -> booleanArrayOf(true, true, false, true)  // T shape (UP, RIGHT, LEFT)
            PipeType.CROSS -> booleanArrayOf(true, true, true, true)
            PipeType.BLANK -> booleanArrayOf(false, false, false, false)
        }

        // Shift array by rotation (each 90deg is 1 cell clockwise shift)
        val shifts = (p.rotationAngle / 90) % 4
        val result = BooleanArray(4)
        for (i in 0..3) {
            result[(i + shifts) % 4] = base[i]
        }
        return result
    }

    // 3. Slide Tile
    fun slideTile(index: Int) {
        val lvl = activeLevel ?: return
        val slideState = lvl.slidePuzzle ?: return
        val size = slideState.size
        val grid = slideState.grid
        val blankIdx = slideState.blankIndex

        val bx = blankIdx % size
        val by = blankIdx / size
        val tx = index % size
        val ty = index / size

        val isAdjacent = (bx == tx && Math.abs(by - ty) == 1) || (by == ty && Math.abs(bx - tx) == 1)

        if (isAdjacent) {
            // Push to undo
            undoStackSlide.push(ArrayList(grid))

            val newGrid = ArrayList(grid)
            val temp = newGrid[blankIdx]
            newGrid[blankIdx] = newGrid[index]
            newGrid[index] = temp

            val newState = slideState.copy(grid = newGrid, blankIndex = index)
            activeLevel = lvl.copy(slidePuzzle = newState)
            movesCount++

            // Check win
            if (checkSlideWin(newGrid, size)) {
                triggerLevelWin()
            } else {
                triggerAutosave()
            }
        }
    }

    private fun checkSlideWin(grid: List<Int>, size: Int): Boolean {
        // Correct order is 1, 2, 3... -1
        for (i in 0 until grid.size - 1) {
            if (grid[i] != i + 1) return false
        }
        return grid.last() == -1
    }

    // 4. Spot Difference
    fun tapSpotDifferenceRightPanel(index: Int) {
        val lvl = activeLevel ?: return
        val diffState = lvl.spotDifference ?: return

        if (diffState.differencesIndices.contains(index)) {
            if (!foundDifferences.contains(index)) {
                foundDifferences.add(index)
                
                // Match visual or display sound triggers
                if (foundDifferences.size == diffState.differencesIndices.size) {
                    triggerLevelWin()
                } else {
                    triggerAutosave()
                }
            }
        } else {
            // penalty move
            movesCount++
        }
    }

    // 5. Memory Match
    fun flipMemoryCard(index: Int) {
        val lvl = activeLevel ?: return
        val memState = lvl.memoryMatch ?: return
        val cards = memState.cards

        if (memoryCheckingMatches) return
        if (cards[index].isMatched || cards[index].isFlipped) return
        if (flippedCardIndices.contains(index)) return

        // Flip card
        val newCards = cards.mapIndexed { idx, card ->
            if (idx == index) card.copy(isFlipped = true) else card
        }
        activeLevel = lvl.copy(memoryMatch = memState.copy(cards = newCards))
        flippedCardIndices.add(index)

        if (flippedCardIndices.size == 2) {
            movesCount++
            memoryCheckingMatches = true
            
            // Check match after delay to allow visual reading
            viewModelScope.launch {
                delay(800)
                val card1 = newCards[flippedCardIndices[0]]
                val card2 = newCards[flippedCardIndices[1]]
                
                val finalCards = if (card1.symbolId == card2.symbolId && card1.colorId == card2.colorId) {
                    // Match found!
                    if (activeGameMode == "time_attack") {
                        timeElapsedSeconds += 5L
                    }
                    newCards.mapIndexed { idx, card ->
                        if (idx == flippedCardIndices[0] || idx == flippedCardIndices[1]) {
                            card.copy(isMatched = true, isFlipped = true)
                        } else card
                    }
                } else {
                    // Turn face down again
                    newCards.mapIndexed { idx, card ->
                        if (idx == flippedCardIndices[0] || idx == flippedCardIndices[1]) {
                            card.copy(isFlipped = false)
                        } else card
                    }
                }

                flippedCardIndices.clear()
                memoryCheckingMatches = false
                
                activeLevel = lvl.copy(memoryMatch = memState.copy(cards = finalCards))

                if (finalCards.all { it.isMatched }) {
                    triggerLevelWin()
                } else {
                    triggerAutosave()
                }
            }
        }
    }

    // --- GAMEPLAY CONTROLS ---

    fun performUndo() {
        val lvl = activeLevel ?: return
        
        when (lvl.category) {
            PuzzleCategory.WATER_SORT -> {
                if (undoStackTubes.isNotEmpty()) {
                    val prev = undoStackTubes.pop()
                    activeLevel = lvl.copy(waterSort = lvl.waterSort?.copy(tubes = prev))
                    movesCount = (movesCount - 1).coerceAtLeast(0)
                    triggerAutosave()
                }
            }
            PuzzleCategory.PIPE_CONNECT -> {
                if (undoStackPipes.isNotEmpty()) {
                    val prev = undoStackPipes.pop()
                    activeLevel = lvl.copy(pipeConnect = lvl.pipeConnect?.copy(grid = prev))
                    movesCount = (movesCount - 1).coerceAtLeast(0)
                    triggerAutosave()
                }
            }
            PuzzleCategory.SLIDE_PUZZLE -> {
                if (undoStackSlide.isNotEmpty()) {
                    val prev = undoStackSlide.pop()
                    val blankIdx = prev.indexOf(-1)
                    activeLevel = lvl.copy(slidePuzzle = lvl.slidePuzzle?.copy(grid = prev, blankIndex = blankIdx))
                    movesCount = (movesCount - 1).coerceAtLeast(0)
                    triggerAutosave()
                }
            }
            else -> {} // Spot Difference & Memory Match don't support simple stack undo
        }
    }

    fun restartActiveLevel() {
        val lvl = activeLevel ?: return
        loadLevel(lvl.levelId)
    }

    fun triggerHint() {
        if (isHintActive) return
        val lvl = activeLevel ?: return
        
        viewModelScope.launch {
            isHintActive = true
            usedHintsCount++
            repository.incrementHintsUsed()

            // Trigger hints visual highlight (simulate lights for 4 seconds)
            delay(4000)
            isHintActive = false
        }
    }

    fun skipActiveLevel() {
        viewModelScope.launch {
            // Deduct skip cost: 20 Coins or 10 Gems
            val costPaid = repository.spendCoins(30) || repository.spendGems(10)
            if (costPaid || activeLevel?.levelId == 1) { // Free skip on demo level 1
                // Skip level successfully
                stopGameplay()
                val lvlId = activeLevel?.levelId ?: 1
                repository.saveLevelCompletion(
                    levelId = lvlId,
                    stars = 1,
                    bestScore = 100,
                    timeTakenSeconds = timeElapsedSeconds.toInt(),
                    usedHints = true
                )
                
                // Setup completed pop-up state
                isLevelCompleted = true
                activeStarsEarned = 1
                bonusCoinsEarned = 30
                bonusGemsEarned = 1
                
                repository.deleteAutosave()
            } else {
                rewardAlertTitle = "Insufficient Currencies"
                spinResultRewardText = "You need 30 Coins or 10 Gems to Skip this level!"
                rewardAlertCoins = 0
                rewardAlertGems = 0
                showRewardAlert = true
            }
        }
    }

    private fun triggerLevelWin() {
        stopGameplay()
        val lvl = activeLevel ?: return

        // Calculate stars based on moves and timer limits
        val stars = when {
            movesCount <= lvl.movesLimit / 2 -> 3
            movesCount <= lvl.movesLimit -> 2
            else -> 1
        }

        activeStarsEarned = stars
        val mode = activeGameMode
        
        if (mode != null) {
            val earnedCoins = getModeCoinsReward(mode, stars)
            val earnedGems = getModeGemsReward(mode, stars)
            bonusCoinsEarned = earnedCoins
            bonusGemsEarned = earnedGems
            
            viewModelScope.launch {
                val currentProgress = repository.getGameModeProgressDirect(mode) 
                    ?: GameModeProgress(modeId = mode, currentLevel = 1, starsEarned = 0, highestScore = 0, totalPlayTimeSeconds = 0, completedCount = 0)
                
                val nextLevel = currentProgress.currentLevel + 1
                val newStars = currentProgress.starsEarned + stars
                val score = 500 - movesCount * 5 - timeElapsedSeconds.toInt()
                val newHighScore = maxOf(currentProgress.highestScore, score)
                val newPlayTime = currentProgress.totalPlayTimeSeconds + timeElapsedSeconds
                val newCompletedCount = currentProgress.completedCount + 1
                
                val updatedProgress = currentProgress.copy(
                    currentLevel = nextLevel,
                    starsEarned = newStars,
                    highestScore = newHighScore,
                    totalPlayTimeSeconds = newPlayTime,
                    completedCount = newCompletedCount
                )
                repository.saveGameModeProgress(updatedProgress)
                
                // Fetch UserProgress to update (add Coins, Gems, XP)
                val progress = repository.getUserProgressDirect() ?: UserProgress()
                val xpGain = 15 * stars
                
                var updatedCoins = progress.coins + earnedCoins
                var updatedGems = progress.gems + earnedGems
                var updatedXp = progress.xp + xpGain
                var updatedLevel = progress.level
                val xpNeeded = updatedLevel * 100
                if (updatedXp >= xpNeeded) {
                    updatedXp -= xpNeeded
                    updatedLevel++
                    updatedCoins += 100
                    updatedGems += 10
                }
                
                repository.saveUserProgress(progress.copy(
                    coins = updatedCoins,
                    gems = updatedGems,
                    xp = updatedXp,
                    level = updatedLevel
                ))
                
                isLevelCompleted = true
            }
        } else {
            bonusCoinsEarned = 50 + stars * 25 + (if (stars == 3) 50 else 0)
            bonusGemsEarned = if (stars == 3) 5 else 2
            
            viewModelScope.launch {
                repository.saveLevelCompletion(
                    levelId = lvl.levelId,
                    stars = stars,
                    bestScore = 500 - movesCount * 5 - timeElapsedSeconds.toInt(),
                    timeTakenSeconds = timeElapsedSeconds.toInt(),
                    usedHints = usedHintsCount > 0
                )
                isLevelCompleted = true

                // Trigger simulated ad occasionally after completing levels (not too frequently)
                if (lvl.levelId % 2 == 0) {
                    delay(500)
                    showSimulatedInterstitialAd = true
                }
            }
        }
    }

    fun getModeCoinsReward(modeId: String, stars: Int): Int {
        val multiplier = when (modeId) {
            "kids" -> 15
            "beginner" -> 25
            "normal" -> 45
            "hard" -> 75
            "expert" -> 120
            "master" -> 200
            "endless" -> 35
            "quick_play" -> 60
            "daily_challenge" -> 150
            "weekly_challenge" -> 350
            "time_attack" -> 80
            "relax" -> 20
            "brain_training" -> 100
            "iq_challenge" -> 200
            "memory" -> 50
            "logic" -> 50
            "pattern" -> 50
            "survival" -> 110
            "practice" -> 5
            "random" -> 55
            "tournament" -> 300
            "seasonal_events" -> 180
            else -> 50
        }
        return multiplier + stars * 10
    }

    fun getModeGemsReward(modeId: String, stars: Int): Int {
        return when (modeId) {
            "kids" -> if (stars == 3) 1 else 0
            "beginner" -> if (stars == 3) 2 else 1
            "normal" -> if (stars == 3) 3 else 1
            "hard" -> if (stars == 3) 6 else 2
            "expert" -> if (stars == 3) 12 else 4
            "master" -> if (stars == 3) 25 else 8
            "endless" -> if (stars == 3) 2 else 1
            "quick_play" -> if (stars == 3) 4 else 1
            "daily_challenge" -> if (stars == 3) 10 else 3
            "weekly_challenge" -> if (stars == 3) 25 else 8
            "time_attack" -> if (stars == 3) 5 else 2
            "relax" -> 0
            "brain_training" -> if (stars == 3) 6 else 2
            "iq_challenge" -> if (stars == 3) 15 else 5
            "memory" -> if (stars == 3) 3 else 1
            "logic" -> if (stars == 3) 3 else 1
            "pattern" -> if (stars == 3) 3 else 1
            "survival" -> if (stars == 3) 8 else 3
            "practice" -> 0
            "random" -> if (stars == 3) 4 else 1
            "tournament" -> if (stars == 3) 20 else 6
            "seasonal_events" -> if (stars == 3) 12 else 4
            else -> if (stars == 3) 4 else 1
        }
    }

    fun triggerSurvivalOrTimeAttackLoss() {
        stopGameplay()
        if (activeGameMode == "survival") {
            survivalLives--
            if (survivalLives <= 0) {
                rewardAlertTitle = "Game Over"
                spinResultRewardText = "You have run out of lives! Survive next time to earn more."
                rewardAlertCoins = 0
                rewardAlertGems = 0
                showRewardAlert = true
                isLevelCompleted = true // Trigger overlay with custom layout
            } else {
                rewardAlertTitle = "Life Lost!"
                spinResultRewardText = "Oops! You lost a life. Remaining lives: $survivalLives"
                rewardAlertCoins = 0
                rewardAlertGems = 0
                showRewardAlert = true
                
                // Reload level
                val mode = activeGameMode
                if (mode != null) loadModeGameplay(mode)
            }
        } else {
            rewardAlertTitle = "Time's Up!"
            spinResultRewardText = "You ran out of time! Speed up your logical thinking to win."
            rewardAlertCoins = 0
            rewardAlertGems = 0
            showRewardAlert = true
            
            // Reload level
            val mode = activeGameMode
            if (mode != null) loadModeGameplay(mode)
        }
    }

    private fun triggerAutosave() {
        val lvl = activeLevel ?: return
        val serialized = getSerializedLevelState(lvl)
        viewModelScope.launch {
            repository.saveAutosaveGame(
                levelId = lvl.levelId,
                category = lvl.category.name,
                serializedState = serialized,
                moves = movesCount,
                hints = usedHintsCount,
                seconds = timeElapsedSeconds
            )
        }
    }

    // Serialization & Deserialization Helpers
    private fun getSerializedLevelState(level: PuzzleLevel): String {
        return when (level.category) {
            PuzzleCategory.WATER_SORT -> {
                level.waterSort?.tubes?.joinToString("|") { tube ->
                    tube.joinToString(",")
                } ?: ""
            }
            PuzzleCategory.PIPE_CONNECT -> {
                level.pipeConnect?.grid?.joinToString("|") { row ->
                    row.joinToString(",") { "${it.type.name}:${it.rotationAngle}" }
                } ?: ""
            }
            PuzzleCategory.SLIDE_PUZZLE -> {
                level.slidePuzzle?.grid?.joinToString(",") ?: ""
            }
            PuzzleCategory.SPOT_DIFFERENCE -> {
                foundDifferences.joinToString(",")
            }
            PuzzleCategory.MEMORY_MATCH -> {
                level.memoryMatch?.cards?.joinToString("|") {
                    "${it.isMatched}:${it.isFlipped}"
                } ?: ""
            }
        }
    }

    private fun restoreLevelStateFromSerialized(base: PuzzleLevel, data: String): PuzzleLevel {
        if (data.isEmpty()) return base
        try {
            return when (base.category) {
                PuzzleCategory.WATER_SORT -> {
                    val tubes = data.split("|").map { tubeStr ->
                        if (tubeStr.isEmpty()) emptyList<Int>()
                        else tubeStr.split(",").map { it.toInt() }
                    }
                    base.copy(waterSort = base.waterSort?.copy(tubes = tubes))
                }
                PuzzleCategory.PIPE_CONNECT -> {
                    val rows = data.split("|")
                    val grid = base.pipeConnect?.grid?.mapIndexed { y, row ->
                        val cells = rows[y].split(",")
                        row.mapIndexed { x, pipe ->
                            val parts = cells[x].split(":")
                            pipe.copy(
                                type = PipeType.valueOf(parts[0]),
                                rotationAngle = parts[1].toInt()
                            )
                        }
                    } ?: emptyList()
                    base.copy(pipeConnect = base.pipeConnect?.copy(grid = grid))
                }
                PuzzleCategory.SLIDE_PUZZLE -> {
                    val list = data.split(",").map { it.toInt() }
                    val blank = list.indexOf(-1)
                    base.copy(slidePuzzle = base.slidePuzzle?.copy(grid = list, blankIndex = blank))
                }
                PuzzleCategory.SPOT_DIFFERENCE -> {
                    val list = data.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
                    foundDifferences.clear()
                    foundDifferences.addAll(list)
                    base
                }
                PuzzleCategory.MEMORY_MATCH -> {
                    val states = data.split("|")
                    val cards = base.memoryMatch?.cards?.mapIndexed { index, card ->
                        val parts = states[index].split(":")
                        card.copy(
                            isMatched = parts[0].toBoolean(),
                            isFlipped = parts[1].toBoolean()
                        )
                    } ?: emptyList()
                    base.copy(memoryMatch = base.memoryMatch?.copy(cards = cards))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return base // fall back to clean base if corrupted
        }
    }

    // --- REWARDS, LUCKY WHEEL & MISSIONS ---

    fun claimMission(id: String) {
        viewModelScope.launch {
            val claim = repository.claimMissionReward(id)
            if (claim != null) {
                rewardAlertTitle = "Mission Completed!"
                spinResultRewardText = "Thank you for completing this mission! You received resources."
                rewardAlertCoins = claim.first
                rewardAlertGems = claim.second
                showRewardAlert = true
            }
        }
    }

    fun claimAchievement(id: String) {
        viewModelScope.launch {
            val gold = repository.claimAchievementReward(id)
            if (gold != null) {
                rewardAlertTitle = "Achievement Unlocked!"
                spinResultRewardText = "A brilliant puzzle explorer is you! Claim your rewards."
                rewardAlertCoins = gold
                rewardAlertGems = 0
                showRewardAlert = true
            }
        }
    }

    fun claimDailyReward(day: Int) {
        viewModelScope.launch {
            val coins = day * 100
            val gems = if (day % 7 == 0) 25 else day * 2
            
            val progress = userProgress.value ?: UserProgress()
            val now = System.currentTimeMillis()
            
            // Check streak rules (cooldown 18 hours to prevent spam, allow once per day)
            val timeDiff = now - progress.lastDailyRewardTime
            if (timeDiff >= 18 * 60 * 60 * 1000L || progress.lastDailyRewardTime == 0L) {
                repository.addRewards(coins, gems)
                repository.saveUserProgress(progress.copy(
                    lastDailyRewardTime = now,
                    dailyRewardStreak = day
                ))
                
                rewardAlertTitle = "Day $day Checked In!"
                spinResultRewardText = "Nice! Keep your daily streak going for amazing bonuses."
                rewardAlertCoins = coins
                rewardAlertGems = gems
                showRewardAlert = true
            } else {
                rewardAlertTitle = "Already Claimed"
                spinResultRewardText = "Come back tomorrow for your next daily reward!"
                rewardAlertCoins = 0
                rewardAlertGems = 0
                showRewardAlert = true
            }
        }
    }

    fun spinLuckyWheel() {
        if (isSpinning) return
        viewModelScope.launch {
            val progress = userProgress.value ?: UserProgress()
            val now = System.currentTimeMillis()
            
            val isFreeSpin = (now - progress.lastSpinTime) >= 20 * 60 * 60 * 1000L || progress.lastSpinTime == 0L
            val costPaid = isFreeSpin || repository.spendGems(5) // Deduct 5 gems if not free spin

            if (costPaid) {
                isSpinning = true
                val rand = Random()
                
                // Pick index deterministically: 8 segments
                val selectedIndex = rand.nextInt(8)
                val baseRotations = 5 * 360f // Spin 5 times around
                val targetAngle = selectedIndex * (360f / 8f)
                val finalAngle = baseRotations + targetAngle

                // Animate spin rotation inside Kotlin
                spinRotationAngle = 0f
                var elapsed = 0
                val duration = 2500 // 2.5 seconds
                while (elapsed < duration) {
                    delay(16)
                    elapsed += 16
                    val percent = elapsed.toFloat() / duration.toFloat()
                    // Decelerate curve
                    val factor = 1f - (1f - percent) * (1f - percent) * (1f - percent)
                    spinRotationAngle = finalAngle * factor
                }

                // Spin complete! Determine reward
                var rewardCoins = 0
                var rewardGems = 0
                var label = ""

                when (selectedIndex) {
                    0 -> { rewardCoins = 100; label = "100 Coins" }
                    1 -> { rewardGems = 5; label = "5 Gems" }
                    2 -> { rewardCoins = 250; label = "250 Coins" }
                    3 -> { rewardGems = 10; label = "10 Gems" }
                    4 -> { rewardCoins = 500; label = "500 Coins" }
                    5 -> { rewardGems = 20; label = "20 Gems" }
                    6 -> { rewardCoins = 1000; label = "Mega 1000 Coins!" }
                    7 -> { rewardGems = 50; label = "Jackpot 50 Gems!" }
                }

                repository.addRewards(rewardCoins, rewardGems)
                repository.incrementLuckyWheelSpins()
                repository.saveUserProgress(
                    (userProgress.value ?: progress).copy(lastSpinTime = if (isFreeSpin) now else progress.lastSpinTime)
                )

                spinResultRewardText = "Congratulations! You landed on: $label"
                rewardAlertTitle = "Lucky Spin Reward!"
                rewardAlertCoins = rewardCoins
                rewardAlertGems = rewardGems
                showRewardAlert = true
                isSpinning = false
            } else {
                rewardAlertTitle = "No Free Spin"
                spinResultRewardText = "You need 5 Gems to spin the Lucky Wheel again!"
                rewardAlertCoins = 0
                rewardAlertGems = 0
                showRewardAlert = true
            }
        }
    }

    // --- SHOP ACTIONS ---

    fun buyCoinsPack(coinAmount: Int, gemCost: Int) {
        viewModelScope.launch {
            if (repository.spendGems(gemCost)) {
                repository.addRewards(coinAmount, 0)
                rewardAlertTitle = "Purchase Successful"
                spinResultRewardText = "Successfully purchased $coinAmount Coins!"
                rewardAlertCoins = coinAmount
                rewardAlertGems = 0
                showRewardAlert = true
            } else {
                rewardAlertTitle = "Insufficient Gems"
                spinResultRewardText = "You need $gemCost Gems to buy this Coins pack!"
                rewardAlertCoins = 0
                rewardAlertGems = 0
                showRewardAlert = true
            }
        }
    }

    fun buyGemsPack(gemAmount: Int, usdPrice: Double) {
        // Since we are simulated offline secure play store game, simulate with coin trade or simulated play store popup!
        viewModelScope.launch {
            val progress = userProgress.value ?: UserProgress()
            repository.saveUserProgress(progress.copy(gems = progress.gems + gemAmount))
            rewardAlertTitle = "Simulated Play Store Success"
            spinResultRewardText = "Thank you for buying $gemAmount Gems ($usdPrice USD paid inside sandbox)!"
            rewardAlertCoins = 0
            rewardAlertGems = gemAmount
            showRewardAlert = true
        }
    }

    fun unlockThemeFromShop(themeName: String, coinCost: Int) {
        viewModelScope.launch {
            val success = repository.unlockTheme(themeName, coinCost)
            if (success) {
                repository.saveThemeSelection(themeName)
                rewardAlertTitle = "Theme Unlocked!"
                spinResultRewardText = "The '$themeName' visual interface has been unlocked and activated!"
                rewardAlertCoins = 0
                rewardAlertGems = 0
                showRewardAlert = true
            } else {
                rewardAlertTitle = "Theme Locked"
                spinResultRewardText = "You need $coinCost Coins to purchase this visual Theme!"
                rewardAlertCoins = 0
                rewardAlertGems = 0
                showRewardAlert = true
            }
        }
    }

    fun removeAdsVIPPremium() {
        viewModelScope.launch {
            val progress = userProgress.value ?: UserProgress()
            repository.saveUserProgress(progress.copy(
                adFreeUnlocked = true,
                doubleCoinsUnlocked = true,
                vipUnlocked = true,
                coins = progress.coins + 5000,
                gems = progress.gems + 200
            ))
            repository.buyThemeDirect("Neon Eclipse")
            repository.buyThemeDirect("Emerald Forest")
            repository.buyThemeDirect("Amber Glow")
            repository.buyThemeDirect("Pastel Dreams")

            rewardAlertTitle = "Premium VIP Activated!"
            spinResultRewardText = "Welcome to Puzzle Nova VIP! All themes unlocked, ads removed, and bonus currencies delivered."
            rewardAlertCoins = 5000
            rewardAlertGems = 200
            showRewardAlert = true
        }
    }

    fun changeActiveTheme(themeName: String) {
        viewModelScope.launch {
            repository.saveThemeSelection(themeName)
        }
    }

    // --- ADS SYSTEM (SIMULATED PLAY STORE READY) ---

    fun showRewardedAdToEarnGems(onCompleted: () -> Unit) {
        onRewardedAdCompleted = onCompleted
        showSimulatedRewardedAd = true
    }

    fun completeSimulatedRewardedAd() {
        showSimulatedRewardedAd = false
        onRewardedAdCompleted?.invoke()
        onRewardedAdCompleted = null
    }

    // --- ADMIN PANEL CONTROL PANEL FUNCTIONS ---

    fun adminSetResources(coinsDelta: Int, gemsDelta: Int) {
        viewModelScope.launch {
            repository.adminAddResources(coinsDelta, gemsDelta)
        }
    }

    fun adminUnlockFirstNLevels(count: Int) {
        viewModelScope.launch {
            repository.adminUnlockLevels(count)
        }
    }

    fun adminResetAllProgress() {
        viewModelScope.launch {
            repository.adminResetProgress()
            activeLevel = null
            isLevelCompleted = false
            selectedTubeIndex = -1
            flippedCardIndices.clear()
            foundDifferences.clear()
        }
    }

    fun updateSettings(sound: Boolean, music: Boolean, vibration: Boolean, kidsMode: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(sound, music, vibration, kidsMode)
        }
    }
}
