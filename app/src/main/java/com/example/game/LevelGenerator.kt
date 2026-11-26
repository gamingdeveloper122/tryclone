package com.example.game

import java.util.Random

// --- Puzzle Categories ---
enum class PuzzleCategory(val displayName: String) {
    WATER_SORT("Water Sort"),
    PIPE_CONNECT("Pipe Connect"),
    SLIDE_PUZZLE("Slide Puzzle"),
    SPOT_DIFFERENCE("Spot Difference"),
    MEMORY_MATCH("Memory Match")
}

// --- Puzzle State Structures ---

// 1. Water Sort State
data class WaterSortState(
    val tubes: List<List<Int>>, // list of tubes, each tube is a list of color IDs (bottom-to-top)
    val capacity: Int = 4,
    val totalColors: Int
)

// 2. Pipe Connect State
enum class PipeType { I, L, T, CROSS, BLANK }
data class Pipe(
    val type: PipeType,
    val rotationAngle: Int, // 0, 90, 180, 270 degrees
    val isSource: Boolean = false,
    val isSink: Boolean = false
)
data class PipeConnectState(
    val grid: List<List<Pipe>>,
    val size: Int,
    val sourceX: Int = 0,
    val sourceY: Int = 0,
    val sinkX: Int,
    val sinkY: Int
)

// 3. Slide Puzzle State
data class SlidePuzzleState(
    val grid: List<Int>, // flat list of size * size, N-1 represents the blank tile (-1 or size*size-1)
    val size: Int,
    val blankIndex: Int
)

// 4. Spot Difference State
data class DifferenceCell(
    val shapeId: Int, // 0 to 5 (e.g., Circle, Square, Triangle, Star, Cross)
    val colorId: Int, // 0 to 5
    val textLabel: String = ""
)
data class SpotDifferenceState(
    val gridLeft: List<DifferenceCell>,
    val gridRight: List<DifferenceCell>,
    val size: Int,
    val differencesIndices: Set<Int> // indices in the flat list where left and right differ
)

// 5. Memory Match State
data class MemoryCard(
    val id: Int,
    val symbolId: Int,
    val colorId: Int,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)
data class MemoryMatchState(
    val cards: List<MemoryCard>,
    val rows: Int,
    val cols: Int,
    val maxMoves: Int
)

// --- Combined Level Configuration ---
data class PuzzleLevel(
    val levelId: Int,
    val category: PuzzleCategory,
    val difficultyName: String, // "Very Easy", "Easy", "Normal", "Medium", "Hard", "Expert", "Master"
    val isUnlocked: Boolean = false,
    val stars: Int = 0,
    val bestScore: Int = 0,
    
    // Concrete Puzzle States
    val waterSort: WaterSortState? = null,
    val pipeConnect: PipeConnectState? = null,
    val slidePuzzle: SlidePuzzleState? = null,
    val spotDifference: SpotDifferenceState? = null,
    val memoryMatch: MemoryMatchState? = null,

    // Limits & Rules
    val movesLimit: Int = 0,
    val timeLimitSeconds: Int = 0, // 0 means no time limit
    val hintString: String = ""
)

object LevelGenerator {

    fun getCategoryForLevel(levelId: Int): PuzzleCategory {
        val categories = PuzzleCategory.values()
        // Simple deterministic assignment to rotate categories
        return categories[(levelId - 1) % categories.size]
    }

    fun getDifficultyName(levelId: Int): String {
        return when (levelId) {
            in 1..30 -> "Very Easy"
            in 31..100 -> "Easy"
            in 101..200 -> "Normal"
            in 201..350 -> "Medium"
            in 351..500 -> "Hard"
            in 501..650 -> "Expert"
            else -> "Master"
        }
    }

    fun generateLevel(levelId: Int): PuzzleLevel {
        val category = getCategoryForLevel(levelId)
        val difficulty = getDifficultyName(levelId)
        val rand = Random(levelId.toLong() * 12345L)

        var waterSort: WaterSortState? = null
        var pipeConnect: PipeConnectState? = null
        var slidePuzzle: SlidePuzzleState? = null
        var spotDifference: SpotDifferenceState? = null
        var memoryMatch: MemoryMatchState? = null

        var movesLimit = 0
        var timeLimit = 0
        var hint = ""

        when (category) {
            PuzzleCategory.WATER_SORT -> {
                // Determine tube configuration
                val colorsCount = when (levelId) {
                    in 1..30 -> 2
                    in 31..100 -> 3
                    in 101..200 -> 4
                    in 201..350 -> 5
                    in 351..500 -> 6
                    in 501..650 -> 7
                    else -> 8
                }
                val emptyTubes = if (colorsCount <= 3) 1 else 2
                val tubesCount = colorsCount + emptyTubes
                
                // Shuffle color segments
                val colorPool = ArrayList<Int>()
                for (colorId in 1..colorsCount) {
                    repeat(4) { colorPool.add(colorId) }
                }
                
                // Deterministic shuffle
                for (i in colorPool.size - 1 downTo 1) {
                    val j = rand.nextInt(i + 1)
                    val temp = colorPool[i]
                    colorPool[i] = colorPool[j]
                    colorPool[j] = temp
                }
                
                val tubes = ArrayList<List<Int>>()
                var poolIndex = 0
                for (t in 0 until colorsCount) {
                    val tube = ArrayList<Int>()
                    repeat(4) {
                        if (poolIndex < colorPool.size) {
                            tube.add(colorPool[poolIndex++])
                        }
                    }
                    tubes.add(tube)
                }
                // Add empty tubes
                repeat(emptyTubes) {
                    tubes.add(emptyList())
                }

                waterSort = WaterSortState(tubes = tubes, totalColors = colorsCount)
                movesLimit = colorsCount * 8 + rand.nextInt(5)
                timeLimit = if (levelId > 30) colorsCount * 30 + 30 else 0
                hint = "Pour segments into empty or matching color tubes until each tube contains only one color!"
            }

            PuzzleCategory.PIPE_CONNECT -> {
                val size = when (levelId) {
                    in 1..30 -> 3
                    in 31..100 -> 4
                    in 101..200 -> 5
                    in 201..350 -> 6
                    in 351..500 -> 7
                    else -> 8
                }
                val sourceX = 0
                val sourceY = 0
                val sinkX = size - 1
                val sinkY = size - 1

                // Create default grid of straight and L-pipes
                val grid = ArrayList<ArrayList<Pipe>>()
                for (y in 0 until size) {
                    val row = ArrayList<Pipe>()
                    for (x in 0 until size) {
                        val isSrc = (x == sourceX && y == sourceY)
                        val isSnk = (x == sinkX && y == sinkY)
                        
                        // Pick random pipe shape
                        val types = listOf(PipeType.I, PipeType.L, PipeType.T, PipeType.CROSS)
                        val pType = if (isSrc || isSnk) PipeType.CROSS else types[rand.nextInt(types.size)]
                        val angle = listOf(0, 90, 180, 270)[rand.nextInt(4)]
                        
                        row.add(Pipe(type = pType, rotationAngle = angle, isSource = isSrc, isSink = isSnk))
                    }
                    grid.add(row)
                }

                // Force a guaranteed path from source to sink in the baseline logic
                // Ensure at least some connected path exists
                var cx = sourceX
                var cy = sourceY
                while (cx != sinkX || cy != sinkY) {
                    if (cx < sinkX && (cy == sinkY || rand.nextBoolean())) {
                        cx++
                    } else {
                        cy++
                    }
                    // Place L or straight pipe to connect
                    if (cx != sinkX || cy != sinkY) {
                        val existing = grid[cy][cx]
                        val pathType = if (rand.nextBoolean()) PipeType.I else PipeType.L
                        grid[cy][cx] = Pipe(
                            type = pathType,
                            rotationAngle = listOf(0, 90, 180, 270)[rand.nextInt(4)],
                            isSource = false,
                            isSink = false
                        )
                    }
                }

                pipeConnect = PipeConnectState(
                    grid = grid,
                    size = size,
                    sourceX = sourceX,
                    sourceY = sourceY,
                    sinkX = sinkX,
                    sinkY = sinkY
                )
                movesLimit = size * 4 + rand.nextInt(5)
                timeLimit = if (levelId > 30) size * 20 else 0
                hint = "Tap pipe joints to rotate them. Connect the blinking Source at Top-Left to the Sink at Bottom-Right!"
            }

            PuzzleCategory.SLIDE_PUZZLE -> {
                val size = when (levelId) {
                    in 1..150 -> 3
                    else -> 4
                }
                val totalTiles = size * size
                val flatGrid = ArrayList<Int>()
                for (i in 0 until totalTiles - 1) {
                    flatGrid.add(i + 1) // tiles 1 to N-1
                }
                flatGrid.add(-1) // -1 represents the blank space

                // Perform deterministic valid slides backward to guarantee solvability
                var blankIdx = totalTiles - 1
                val shufflesCount = (levelId * 3).coerceIn(10, 60)
                
                repeat(shufflesCount) {
                    val validMoves = ArrayList<Int>()
                    val bx = blankIdx % size
                    val by = blankIdx / size

                    if (bx > 0) validMoves.add(blankIdx - 1) // left
                    if (bx < size - 1) validMoves.add(blankIdx + 1) // right
                    if (by > 0) validMoves.add(blankIdx - size) // up
                    if (by < size - 1) validMoves.add(blankIdx + size) // down

                    val moveToSwap = validMoves[rand.nextInt(validMoves.size)]
                    // swap
                    val temp = flatGrid[blankIdx]
                    flatGrid[blankIdx] = flatGrid[moveToSwap]
                    flatGrid[moveToSwap] = temp
                    blankIdx = moveToSwap
                }

                slidePuzzle = SlidePuzzleState(grid = flatGrid, size = size, blankIndex = blankIdx)
                movesLimit = shufflesCount + 15
                timeLimit = if (levelId > 30) size * 45 else 0
                hint = "Slide adjacent numbered tiles into the blank tile until all numbers are ordered in sequence!"
            }

            PuzzleCategory.SPOT_DIFFERENCE -> {
                val size = when (levelId) {
                    in 1..100 -> 3
                    in 101..350 -> 4
                    else -> 5
                }
                
                val cellCount = size * size
                val gridLeft = ArrayList<DifferenceCell>()
                val gridRight = ArrayList<DifferenceCell>()

                for (i in 0 until cellCount) {
                    val shapeId = rand.nextInt(6) // 6 distinct shapes
                    val colorId = rand.nextInt(6) // 6 colors
                    val cell = DifferenceCell(shapeId = shapeId, colorId = colorId)
                    gridLeft.add(cell)
                    gridRight.add(cell) // make them identical initially
                }

                // Inject a few differences
                val diffCount = when (levelId) {
                    in 1..50 -> 1
                    in 51..200 -> 2
                    in 201..500 -> 3
                    else -> 4
                }

                val diffIndices = mutableSetOf<Int>()
                while (diffIndices.size < diffCount) {
                    val idx = rand.nextInt(cellCount)
                    diffIndices.add(idx)
                }

                for (idx in diffIndices) {
                    val orig = gridLeft[idx]
                    var newShape = rand.nextInt(6)
                    var newColor = rand.nextInt(6)
                    while (newShape == orig.shapeId && newColor == orig.colorId) {
                        newShape = rand.nextInt(6)
                        newColor = rand.nextInt(6)
                    }
                    gridRight[idx] = DifferenceCell(shapeId = newShape, colorId = newColor)
                }

                spotDifference = SpotDifferenceState(
                    gridLeft = gridLeft,
                    gridRight = gridRight,
                    size = size,
                    differencesIndices = diffIndices
                )
                movesLimit = diffCount + 4
                timeLimit = if (levelId > 30) 45 + diffCount * 15 else 0
                hint = "Scan both panels! Tap on the matching tiles in the right panel that have a different color or shape."
            }

            PuzzleCategory.MEMORY_MATCH -> {
                val (rows, cols) = when (levelId) {
                    in 1..40 -> Pair(2, 3)  // 6 cards (3 pairs)
                    in 41..120 -> Pair(3, 4) // 12 cards (6 pairs)
                    in 121..300 -> Pair(4, 4) // 16 cards (8 pairs)
                    in 301..550 -> Pair(4, 5) // 20 cards (10 pairs)
                    else -> Pair(5, 6)       // 30 cards (15 pairs)
                }

                val totalCards = rows * cols
                val totalPairs = totalCards / 2
                
                // Collect symbols and colors for pairs
                val pool = ArrayList<Pair<Int, Int>>()
                for (p in 0 until totalPairs) {
                    val sym = p % 8 // limit to 8 unique icons
                    val col = p % 6 // limit to 6 colors
                    pool.add(Pair(sym, col))
                    pool.add(Pair(sym, col)) // duplicate
                }

                // Shuffle pool
                for (i in pool.size - 1 downTo 1) {
                    val j = rand.nextInt(i + 1)
                    val temp = pool[i]
                    pool[i] = pool[j]
                    pool[j] = temp
                }

                val cardsList = ArrayList<MemoryCard>()
                for (i in 0 until totalCards) {
                    val p = pool[i]
                    cardsList.add(MemoryCard(id = i, symbolId = p.first, colorId = p.second))
                }

                memoryMatch = MemoryMatchState(
                    cards = cardsList,
                    rows = rows,
                    cols = cols,
                    maxMoves = totalPairs * 3
                )
                movesLimit = totalPairs * 3
                timeLimit = if (levelId > 30) totalPairs * 12 else 0
                hint = "Flip cards and find pairs! Memorize their locations to find all pairs within the moves limit."
            }
        }

        return PuzzleLevel(
            levelId = levelId,
            category = category,
            difficultyName = difficulty,
            waterSort = waterSort,
            pipeConnect = pipeConnect,
            slidePuzzle = slidePuzzle,
            spotDifference = spotDifference,
            memoryMatch = memoryMatch,
            movesLimit = movesLimit,
            timeLimitSeconds = timeLimit,
            hintString = hint
        )
    }

    fun generateLevelForMode(modeId: String, levelId: Int): PuzzleLevel {
        val rand = Random(levelId.toLong() * 98765L + modeId.hashCode().toLong())
        
        // 1. Determine category for specific modes
        val category = when (modeId) {
            "memory" -> PuzzleCategory.MEMORY_MATCH
            "logic" -> if (rand.nextBoolean()) PuzzleCategory.WATER_SORT else PuzzleCategory.PIPE_CONNECT
            "pattern" -> if (rand.nextBoolean()) PuzzleCategory.SLIDE_PUZZLE else PuzzleCategory.SPOT_DIFFERENCE
            "iq_challenge" -> listOf(PuzzleCategory.PIPE_CONNECT, PuzzleCategory.SLIDE_PUZZLE, PuzzleCategory.WATER_SORT)[rand.nextInt(3)]
            "kids" -> listOf(PuzzleCategory.WATER_SORT, PuzzleCategory.MEMORY_MATCH, PuzzleCategory.SPOT_DIFFERENCE)[rand.nextInt(3)]
            else -> getCategoryForLevel(levelId)
        }
        
        // 2. Generate mapped difficulty index
        val mappedLevelId = when (modeId) {
            "kids" -> rand.nextInt(15) + 1
            "beginner" -> rand.nextInt(30) + 1
            "normal" -> rand.nextInt(70) + 31
            "hard" -> rand.nextInt(150) + 201
            "expert" -> rand.nextInt(150) + 501
            "master" -> rand.nextInt(100) + 651
            "iq_challenge" -> rand.nextInt(150) + 501
            "time_attack" -> rand.nextInt(100) + 101
            "relax" -> rand.nextInt(100) + 101
            "survival" -> rand.nextInt(150) + 351
            else -> levelId
        }
        
        // Find a level that matches the category near the mappedLevelId
        var adjustedLevel = generateLevel(mappedLevelId)
        if (adjustedLevel.category != category) {
            for (offset in 0..15) {
                val candidate = generateLevel(maxOf(1, mappedLevelId + offset))
                if (candidate.category == category) {
                    adjustedLevel = candidate
                    break
                }
            }
        }
        
        // 3. Override limits and difficulty tags
        val finalDifficulty = when (modeId) {
            "kids" -> "Very Easy (Kids)"
            "beginner" -> "Beginner"
            "normal" -> "Normal"
            "hard" -> "Hard"
            "expert" -> "Expert"
            "master" -> "Master"
            "endless" -> "Endless Lvl $levelId"
            "quick_play" -> "Quick Challenge"
            "daily_challenge" -> "Daily Challenge"
            "weekly_challenge" -> "Weekly Boss"
            "time_attack" -> "Time Attack"
            "relax" -> "Relax Mode"
            "brain_training" -> "Brain Power"
            "iq_challenge" -> "IQ Genius"
            "memory" -> "Memory Fit Lvl $levelId"
            "logic" -> "Logic Lab Lvl $levelId"
            "pattern" -> "Pattern Match Lvl $levelId"
            "survival" -> "Survival Trial"
            "practice" -> "Practice"
            "random" -> "Chaos Random"
            "tournament" -> "Tournament"
            "seasonal_events" -> "Nebula Eclipse"
            else -> adjustedLevel.difficultyName
        }
        
        val finalMovesLimit = when (modeId) {
            "kids" -> 999
            "relax" -> 999
            "practice" -> 999
            "expert" -> (adjustedLevel.movesLimit * 0.8).toInt().coerceAtLeast(3)
            "master" -> (adjustedLevel.movesLimit * 0.7).toInt().coerceAtLeast(2)
            "iq_challenge" -> (adjustedLevel.movesLimit * 0.6).toInt().coerceAtLeast(2)
            else -> adjustedLevel.movesLimit
        }
        
        val finalTimeLimit = when (modeId) {
            "kids" -> 0
            "relax" -> 0
            "practice" -> 0
            "time_attack" -> 45
            "expert" -> 45
            "master" -> 30
            "iq_challenge" -> 30
            else -> adjustedLevel.timeLimitSeconds
        }
        
        return adjustedLevel.copy(
            levelId = levelId,
            difficultyName = finalDifficulty,
            movesLimit = finalMovesLimit,
            timeLimitSeconds = finalTimeLimit
        )
    }
}
