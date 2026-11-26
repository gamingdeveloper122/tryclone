package com.example.game.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.*
import com.example.data.database.UserProgress
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.ui.theme.PuzzleColors

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameplayScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level = viewModel.activeLevel ?: return
    val moves = viewModel.movesCount
    val time = viewModel.timeElapsedSeconds
    val isCompleted = viewModel.isLevelCompleted

    var showPauseMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("game_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to main screen",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Level ${level.levelId}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = level.category.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showPauseMenu = true },
                    modifier = Modifier.testTag("game_pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- LEVEL STATS PILLS ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Moves Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎮  ", fontSize = 14.sp)
                        Text(
                            text = "Moves: $moves",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Timer Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏱️  ", fontSize = 14.sp)
                        Text(
                            text = formatTime(time),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Star Meter (Dynamic stars status)
                val starsCount = when {
                    moves <= level.movesLimit / 2 -> 3
                    moves <= level.movesLimit -> 2
                    else -> 1
                }
                StarRatingDisplay(stars = starsCount, size = 18)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- MAIN PUZZLE INTERACTIVE BOARD ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (level.category) {
                    PuzzleCategory.WATER_SORT -> {
                        RenderWaterSort(
                            state = level.waterSort ?: return@Box,
                            selectedIdx = viewModel.selectedTubeIndex,
                            onTubeClick = { viewModel.selectWaterSortTube(it) }
                        )
                    }
                    PuzzleCategory.PIPE_CONNECT -> {
                        RenderPipeConnect(
                            state = level.pipeConnect ?: return@Box,
                            onPipeClick = { r, c -> viewModel.rotatePipe(r, c) }
                        )
                    }
                    PuzzleCategory.SLIDE_PUZZLE -> {
                        RenderSlidePuzzle(
                            state = level.slidePuzzle ?: return@Box,
                            onTileClick = { viewModel.slideTile(it) }
                        )
                    }
                    PuzzleCategory.SPOT_DIFFERENCE -> {
                        RenderSpotDifference(
                            state = level.spotDifference ?: return@Box,
                            foundIndices = viewModel.foundDifferences,
                            onRightCellClick = { viewModel.tapSpotDifferenceRightPanel(it) }
                        )
                    }
                    PuzzleCategory.MEMORY_MATCH -> {
                        RenderMemoryMatch(
                            state = level.memoryMatch ?: return@Box,
                            onCardClick = { viewModel.flipMemoryCard(it) }
                        )
                    }
                }

                // Glowing Highlight when Hint is active
                if (viewModel.isHintActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💡 ${level.hintString}",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                                .fillMaxWidth(0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTTOM TOOLBAR CONTROLS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo Move Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(
                        onClick = { viewModel.performUndo() },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.testTag("game_undo_button")
                    ) {
                        Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo")
                    }
                    Text("Undo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Hint Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(
                        onClick = { viewModel.triggerHint() },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.testTag("game_hint_button")
                    ) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Hint")
                    }
                    Text("Hint", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Restart Level Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(
                        onClick = { viewModel.restartActiveLevel() },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.testTag("game_restart_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restart")
                    }
                    Text("Restart", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Skip Level Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(
                        onClick = { viewModel.skipActiveLevel() },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.testTag("game_skip_button")
                    ) {
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = "Skip Level")
                    }
                    Text("Skip", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // --- PAUSE MENU DIALOG OVERLAY ---
        if (showPauseMenu) {
            Dialog(onDismissRequest = { showPauseMenu = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val progress = viewModel.userProgress.collectAsState().value ?: UserProgress()

                        // Sound, Music, Vibration toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sound FX", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = progress.soundEnabled,
                                onCheckedChange = {
                                    viewModel.viewModelScope.launch {
                                        viewModel.updateSettings(
                                            sound = it,
                                            music = progress.musicEnabled,
                                            vibration = progress.vibrationEnabled,
                                            kidsMode = progress.kidsModeEnabled
                                        )
                                    }
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Music Loops", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = progress.musicEnabled,
                                onCheckedChange = {
                                    viewModel.viewModelScope.launch {
                                        viewModel.updateSettings(
                                            sound = progress.soundEnabled,
                                            music = it,
                                            vibration = progress.vibrationEnabled,
                                            kidsMode = progress.kidsModeEnabled
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Resume Button
                        Button(
                            onClick = { showPauseMenu = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RESUME", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Restart Button
                        OutlinedButton(
                            onClick = {
                                showPauseMenu = false
                                viewModel.restartActiveLevel()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RESTART LEVEL", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Quit Button
                        TextButton(
                            onClick = {
                                showPauseMenu = false
                                viewModel.stopGameplay()
                                onBack()
                            }
                        ) {
                            Text("EXIT TO MENU", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- LEVEL COMPLETED CELEBRATION DIALOG OVERLAY ---
        if (isCompleted) {
            Dialog(onDismissRequest = {}) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 PUZZLE SOLVED! 🎉",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Stars Display
                        StarRatingDisplay(stars = viewModel.activeStarsEarned, size = 36)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Superb! You solved the level in $moves moves and ${formatTime(time)}!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Rewards Section
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("BONUS REWARDS RECEIVED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🪙", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+${viewModel.bonusCoinsEarned}", fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💎", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+${viewModel.bonusGemsEarned}", fontWeight = FontWeight.Bold, color = Color(0xFFD946EF))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Primary "Next Level" button
                        Button(
                            onClick = {
                                val nextLevelId = level.levelId + 1
                                viewModel.loadLevel(nextLevelId)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("next_level_button")
                        ) {
                            Text("NEXT LEVEL ➔", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RETURN TO HOME", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-RENDERERS FOR PUZZLES ---

// 1. Water Sort Board Renderer
@Composable
fun RenderWaterSort(
    state: WaterSortState,
    selectedIdx: Int,
    onTubeClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        state.tubes.forEachIndexed { idx, tube ->
            val isSelected = selectedIdx == idx
            val offsetVal = if (isSelected) (-16).dp else 0.dp

            Column(
                modifier = Modifier
                    .offset(y = offsetVal)
                    .width(46.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .border(
                        width = if (isSelected) 3.dp else 2.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onTubeClick(idx) }
                    .padding(4.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Render colored segment capsules inside tube
                tube.forEach { colorId ->
                    val color = PuzzleColors[colorId % PuzzleColors.size]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 1.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }

                // Fill blank height spaces if tube is not fully filled
                val emptySlots = state.capacity - tube.size
                if (emptySlots > 0) {
                    Spacer(modifier = Modifier.weight(emptySlots.toFloat()))
                }
            }
        }
    }
}

// 2. Pipe Connect Canvas Renderer
@Composable
fun RenderPipeConnect(
    state: PipeConnectState,
    onPipeClick: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (y in 0 until state.size) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    for (x in 0 until state.size) {
                        val pipe = state.grid[y][x]
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(1.dp, Color.DarkGray.copy(alpha = 0.3f))
                                .clickable { onPipeClick(y, x) },
                            contentAlignment = Alignment.Center
                        ) {
                            // Custom Canvas to draw responsive vector pipes
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val radius = size.width / 2
                                val thickness = 8.dp.toPx()

                                val defaultColor = Color.DarkGray
                                val powerColor = Color(0xFF00E5FF) // neon cyan

                                val isSourceOrSink = pipe.isSource || pipe.isSink
                                val color = if (isSourceOrSink) powerColor else defaultColor

                                // Helper directions coordinate draw
                                when (pipe.type) {
                                    PipeType.I -> {
                                        // Straight line
                                        val angleRad = Math.toRadians(pipe.rotationAngle.toDouble())
                                        val dx = Math.sin(angleRad).toFloat() * radius
                                        val dy = Math.cos(angleRad).toFloat() * radius
                                        
                                        drawLine(
                                            color = color,
                                            start = Offset(center.x - dx, center.y + dy),
                                            end = Offset(center.x + dx, center.y - dy),
                                            strokeWidth = thickness,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                    PipeType.L -> {
                                        // Draw two segments joining at center based on angle
                                        val angle = pipe.rotationAngle
                                        val top = Offset(center.x, 0f)
                                        val right = Offset(size.width, center.y)
                                        val bottom = Offset(center.x, size.height)
                                        val left = Offset(0f, center.y)

                                        val joints = when (angle) {
                                            0 -> Pair(top, right)
                                            90 -> Pair(right, bottom)
                                            180 -> Pair(bottom, left)
                                            else -> Pair(left, top)
                                        }

                                        drawLine(color = color, start = joints.first, end = center, strokeWidth = thickness, cap = StrokeCap.Round)
                                        drawLine(color = color, start = joints.second, end = center, strokeWidth = thickness, cap = StrokeCap.Round)
                                    }
                                    PipeType.T -> {
                                        // 3 way joint
                                        val angle = pipe.rotationAngle
                                        val top = Offset(center.x, 0f)
                                        val right = Offset(size.width, center.y)
                                        val bottom = Offset(center.x, size.height)
                                        val left = Offset(0f, center.y)

                                        val outputs = when (angle) {
                                            0 -> listOf(left, top, right)
                                            90 -> listOf(top, right, bottom)
                                            180 -> listOf(right, bottom, left)
                                            else -> listOf(bottom, left, top)
                                        }

                                        for (out in outputs) {
                                            drawLine(color = color, start = out, end = center, strokeWidth = thickness)
                                        }
                                    }
                                    PipeType.CROSS -> {
                                        drawLine(color = color, start = Offset(center.x, 0f), end = Offset(center.x, size.height), strokeWidth = thickness)
                                        drawLine(color = color, start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = thickness)
                                    }
                                    PipeType.BLANK -> {
                                        // Draw centered dot
                                        drawCircle(color = Color.Gray.copy(alpha = 0.3f), radius = 4f, center = center)
                                    }
                                }

                                // If source or sink, draw nice glowing node
                                if (pipe.isSource) {
                                    drawCircle(
                                        color = Color(0xFF00FF87), // Green power source
                                        radius = thickness * 1.5f,
                                        center = center
                                    )
                                    drawCircle(color = Color.White, radius = thickness * 0.7f, center = center)
                                } else if (pipe.isSink) {
                                    drawCircle(
                                        color = Color(0xFF00E5FF), // Blue power sink
                                        radius = thickness * 1.5f,
                                        center = center
                                    )
                                    drawCircle(color = Color.White, radius = thickness * 0.7f, center = center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Slide Puzzle Grid Renderer
@Composable
fun RenderSlidePuzzle(
    state: SlidePuzzleState,
    onTileClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(12.dp)
    ) {
        val columns = state.size
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.grid.size) { index ->
                val tileVal = state.grid[index]

                if (tileVal == -1) {
                    // Blank tile
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clickable { onTileClick(index) }
                            .testTag("slide_tile_$tileVal")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tileVal.toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. Spot Difference comparative side-by-side grids
@Composable
fun RenderSpotDifference(
    state: SpotDifferenceState,
    foundIndices: List<Int>,
    onRightCellClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Top/Left Panel: Base Grid (Read Only)
        Text("ORIGINAL GRID (REFERENCE)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(state.size),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.gridLeft.size) { index ->
                    val cell = state.gridLeft[index]
                    DifferenceGridItem(cell = cell, isFound = false)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom/Right Panel: Tappable Target Panel
        Text("TAP ➔ SPOT ALL DIFFERENCES HERE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(state.size),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.gridRight.size) { index ->
                    val cell = state.gridRight[index]
                    val isFound = foundIndices.contains(index)
                    
                    Box(
                        modifier = Modifier
                            .clickable { onRightCellClick(index) }
                            .testTag("diff_cell_$index")
                    ) {
                        DifferenceGridItem(cell = cell, isFound = isFound)
                        
                        // Check overlay
                        if (isFound) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Green.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, tint = Color.Green, contentDescription = "Matched")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DifferenceGridItem(cell: DifferenceCell, isFound: Boolean) {
    val color = PuzzleColors[cell.colorId % PuzzleColors.size]
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val rad = size.width * 0.4f
            
            when (cell.shapeId) {
                0 -> drawCircle(color = color, radius = rad, center = center) // Circle
                1 -> drawRect(color = color, topLeft = Offset(center.x - rad, center.y - rad), size = Size(rad*2, rad*2)) // Square
                2 -> {
                    // Triangle
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x, center.y - rad)
                        lineTo(center.x + rad, center.y + rad)
                        lineTo(center.x - rad, center.y + rad)
                        close()
                    }
                    drawPath(path = path, color = color)
                }
                3 -> {
                    // Star Cross Shape representation
                    drawLine(color = color, start = Offset(center.x - rad, center.y), end = Offset(center.x + rad, center.y), strokeWidth = 8f)
                    drawLine(color = color, start = Offset(center.x, center.y - rad), end = Offset(center.x, center.y + rad), strokeWidth = 8f)
                }
                4 -> {
                    // Hexagon Ring representation
                    drawCircle(color = color, radius = rad, center = center, style = Stroke(width = 8f))
                }
                else -> {
                    // Filled Donut representation
                    drawCircle(color = color, radius = rad, center = center)
                    drawCircle(color = Color.Black, radius = rad * 0.4f, center = center)
                }
            }
        }
    }
}

// 5. Memory Match Cards Renderer
@Composable
fun RenderMemoryMatch(
    state: MemoryMatchState,
    onCardClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(state.cols),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.cards.size) { index ->
            val card = state.cards[index]
            val isShown = card.isFlipped || card.isMatched

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isShown) Color.White.copy(alpha = 0.1f)
                                     else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .aspectRatio(1f)
                    .border(
                        width = 1.5.dp,
                        color = if (isShown) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .shadow(1.dp, RoundedCornerShape(12.dp))
                    .clickable { onCardClick(index) }
                    .testTag("memory_card_$index")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isShown) {
                        val symbolColor = PuzzleColors[card.colorId % PuzzleColors.size]
                        
                        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val rad = size.width * 0.35f
                            
                            when (card.symbolId) {
                                0 -> drawCircle(color = symbolColor, radius = rad, center = center)
                                1 -> drawRect(color = symbolColor, topLeft = Offset(center.x - rad, center.y - rad), size = Size(rad*2, rad*2))
                                2 -> {
                                    val path = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(center.x, center.y - rad)
                                        lineTo(center.x + rad, center.y + rad)
                                        lineTo(center.x - rad, center.y + rad)
                                        close()
                                    }
                                    drawPath(path = path, color = symbolColor)
                                }
                                3 -> {
                                    drawLine(color = symbolColor, start = Offset(center.x - rad, center.y), end = Offset(center.x + rad, center.y), strokeWidth = 8f)
                                    drawLine(color = symbolColor, start = Offset(center.x, center.y - rad), end = Offset(center.x, center.y + rad), strokeWidth = 8f)
                                }
                                4 -> drawCircle(color = symbolColor, radius = rad, center = center, style = Stroke(width = 6f))
                                else -> {
                                    drawCircle(color = symbolColor, radius = rad, center = center)
                                    drawCircle(color = Color.Black, radius = rad * 0.3f, center = center)
                                }
                            }
                        }
                    } else {
                        // Face down: render cool geometric lock/stars pattern
                        Text(
                            text = "❓",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// --- Time Formatter Utility ---
fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
