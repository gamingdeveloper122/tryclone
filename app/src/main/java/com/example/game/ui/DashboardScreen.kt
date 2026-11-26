package com.example.game.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.database.*
import com.example.game.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: GameViewModel,
    onPlayLevel: (Int) -> Unit,
    onPlayMode: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progress = viewModel.userProgress.collectAsState().value ?: UserProgress()
    val levelProgress = viewModel.levelProgressList.collectAsState().value

    var currentTab by remember { mutableStateOf(0) } // 0: Home, 1: Levels, 2: Missions, 3: Shop, 4: Extras, 5: Settings
    var showAdminPanel by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showGameModesScreen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .border(width = 1.dp, color = Color(0xFF334155).copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Left profile section
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFF9333EA))
                                    )
                                )
                                .border(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "JD",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                        // Online indicator dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color(0xFF22C55E), CircleShape)
                                .border(1.5.dp, Color(0xFF0B0E14), CircleShape)
                        )
                    }
                    
                    Column {
                        Text(
                            "EXP. LV. ${progress.level}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                "StarSeeker",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFF1F5F9)
                            )
                            Text("★", fontSize = 10.sp, color = Color(0xFF6366F1))
                        }
                    }
                }

                // Center: Dedicated official logo area with subtle glow effect
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("header_logo_area"),
                    contentAlignment = Alignment.Center
                ) {
                    // Subtle neon glow halo behind the logo card
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFD946EF).copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                    )
                    Card(
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.puzzle_nova_banner),
                            contentDescription = "Puzzle Nova Header Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Right: Resources Pills, Settings & Admin
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Coins Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0B0E14))
                            .border(1.dp, Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${progress.coins}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = Color(0xFFFFC107)
                        )
                    }

                    // Gems Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0B0E14))
                            .border(1.dp, Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💎", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${progress.gems}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = Color(0xFFE040FB)
                        )
                    }

                    // Gear Settings Icon
                    IconButton(
                        onClick = { 
                            showSettingsScreen = !showSettingsScreen 
                            if (showSettingsScreen) {
                                showAdminPanel = false
                                showGameModesScreen = false
                            }
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (showSettingsScreen) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Gear Admin Icon
                    IconButton(
                        onClick = { 
                            showAdminPanel = !showAdminPanel 
                            if (showAdminPanel) {
                                showSettingsScreen = false
                                showGameModesScreen = false
                            }
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Developer Control Panel",
                            tint = if (showAdminPanel) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .border(width = 1.dp, color = Color(0xFF1E293B).copy(alpha = 0.3f))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF161B26))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item 1: Levels
                    BottomNavItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = Icons.Default.GridView,
                        label = "Levels",
                        tag = "nav_tab_levels"
                    )

                    // Item 2: Missions
                    BottomNavItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = Icons.Default.Assignment,
                        label = "Missions",
                        tag = "nav_tab_missions"
                    )

                    // Item 3: Home (Floating Highlight)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(8.dp, CircleShape, clip = false)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                                )
                            )
                            .border(1.dp, Color(0xFF818CF8).copy(alpha = 0.3f), CircleShape)
                            .clickable { currentTab = 0 }
                            .testTag("nav_tab_home"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Item 4: Shop
                    BottomNavItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = Icons.Default.ShoppingBag,
                        label = "Shop",
                        tag = "nav_tab_shop"
                    )

                    // Item 5: Extras
                    BottomNavItem(
                        selected = currentTab == 4,
                        onClick = { currentTab = 4 },
                        icon = Icons.Default.WorkspacePremium,
                        label = "Extras",
                        tag = "nav_tab_extras"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF6366F1).copy(alpha = 0.08f), Color.Transparent),
                            radius = size.minDimension * 0.6f
                        ),
                        center = Offset(size.width * 0.2f, size.height * 0.3f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF9333EA).copy(alpha = 0.08f), Color.Transparent),
                            radius = size.minDimension * 0.6f
                        ),
                        center = Offset(size.width * 0.8f, size.height * 0.7f)
                    )
                }
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Render Active Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (showAdminPanel) {
                        AdminPanelPanel(viewModel = viewModel)
                    } else if (showSettingsScreen) {
                        SettingsScreen(
                            viewModel = viewModel,
                            progress = progress,
                            onBack = { showSettingsScreen = false }
                        )
                    } else if (showGameModesScreen) {
                        GameModesScreen(
                            viewModel = viewModel,
                            progress = progress,
                            onPlayMode = onPlayMode,
                            onBack = { showGameModesScreen = false }
                        )
                    } else {
                        when (currentTab) {
                            0 -> HomePanel(
                                progress = progress,
                                levelProgress = levelProgress,
                                viewModel = viewModel,
                                onPlayLevel = onPlayLevel,
                                onNavigateToLevels = { currentTab = 1 },
                                onExploreModes = { showGameModesScreen = true }
                            )
                            1 -> LevelsPanel(
                                progress = progress,
                                levelProgress = levelProgress,
                                onPlayLevel = onPlayLevel
                            )
                            2 -> MissionsPanel(
                                viewModel = viewModel
                            )
                            3 -> ShopPanel(
                                progress = progress,
                                viewModel = viewModel
                            )
                            4 -> ExtrasPanel(
                                progress = progress,
                                levelProgress = levelProgress,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }

            // Global Reward Popup Alert Trigger
            if (viewModel.showRewardAlert) {
                RewardDialog(
                    title = viewModel.rewardAlertTitle,
                    subtitle = viewModel.spinResultRewardText,
                    coins = viewModel.rewardAlertCoins,
                    gems = viewModel.rewardAlertGems,
                    onDismiss = { viewModel.showRewardAlert = false }
                )
            }

            // Simulated Ads popups
            if (viewModel.showSimulatedInterstitialAd) {
                SimulatedAdDialog(adUnitType = "Interstitial") {
                    viewModel.showSimulatedInterstitialAd = false
                }
            }
            if (viewModel.showSimulatedRewardedAd) {
                SimulatedAdDialog(adUnitType = "Rewarded Video") { completed ->
                    if (completed) {
                        viewModel.completeSimulatedRewardedAd()
                    } else {
                        viewModel.showSimulatedRewardedAd = false
                    }
                }
            }
        }
    }
}

// --- CURRENCY STATS HEADS-UP BAR ---
@Composable
fun WalletStatusBar(progress: UserProgress, viewModel: GameViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coins Pill
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🪙", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${progress.coins}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color(0xFFFFC107)
            )
        }

        // Gems Pill
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💎", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${progress.gems}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color(0xFFE040FB)
            )
        }

        // Energy Pill
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚡", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${progress.energy}/100",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color(0xFF00E5FF)
            )
        }
    }
}

// =================== PANELS IMPLEMENTATIONS ===================

// --- 1. HOME PANEL ---
@Composable
fun HomePanel(
    progress: UserProgress,
    levelProgress: List<LevelProgress>,
    viewModel: GameViewModel,
    onPlayLevel: (Int) -> Unit,
    onNavigateToLevels: () -> Unit,
    onExploreModes: () -> Unit
) {
    val activeSave = viewModel.savedGame.collectAsState().value
    val nextPlayLevel = progress.statsLevelsCompleted + 1

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Hero Marketing Graphic Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.puzzle_nova_banner),
                    contentDescription = "Puzzle Nova Star Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay text inside banner
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = "COSMIC PUZZLES UNLOCKED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Solve sorting, piping, sliding challenges!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Official Puzzle Nova Logo Branding Block on Main Menu
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("main_menu_logo_block"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glowing Official Logo Centerpiece
                Box(
                    modifier = Modifier.size(190.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Cosmic radial aura glow
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFD946EF).copy(alpha = 0.25f),
                                        Color(0xFF818CF8).copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f)), // Gold Trim
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        modifier = Modifier.size(140.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.puzzle_nova_banner),
                            contentDescription = "Puzzle Nova Official Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "THE INFINITE ODYSSEY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF818CF8)
                )
            }
        }

        // Active Save Continue vs Play next level Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activeSave != null && activeSave.active) {
                    GlowButton(
                        text = "CONTINUE Lvl ${activeSave.levelId}",
                        onClick = { onPlayLevel(activeSave.levelId) },
                        modifier = Modifier.weight(1f),
                        primaryColor = MaterialTheme.colorScheme.secondary,
                        accentColor = Color(0xFFE040FB),
                        tag = "continue_game_button"
                    )
                }

                GlowButton(
                    text = "PLAY LEVEL $nextPlayLevel",
                    onClick = { onPlayLevel(nextPlayLevel) },
                    modifier = Modifier.weight(1f),
                    tag = "play_next_level_button"
                )
            }
        }

        // Player Level XP progress card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${progress.level}",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rank: Nova Apprentice", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // XP Bar progress
                        val xpNeeded = progress.level * 100
                        val xpFraction = progress.xp.toFloat() / xpNeeded.toFloat()
                        
                        LinearProgressIndicator(
                            progress = { xpFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${progress.xp} / $xpNeeded XP to level up", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Game Modes Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B).copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFFC084FC)))),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExploreModes() }
                    .testTag("explore_game_modes_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFFC084FC).copy(alpha = 0.25f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌌", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MULTIPLE GAME MODES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFC084FC),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Explore 23 Specialized Modes",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kids, Survival, Time Attack, Challenges & more!",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Explore",
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Daily Check-In Calendar Panel
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "📅 DAILY CHECK-IN REWARDS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val days = listOf(1, 2, 3, 4, 5, 6, 7)
                    days.forEach { day ->
                        val isClaimed = progress.dailyRewardStreak >= day
                        val isToday = progress.dailyRewardStreak + 1 == day

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.8f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isClaimed -> Color.Gray.copy(alpha = 0.2f)
                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .border(
                                    width = if (isToday) 2.dp else 1.dp,
                                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { if (isToday) viewModel.claimDailyReward(day) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Day $day", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isToday) MaterialTheme.colorScheme.primary else Color.Gray)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(if (day == 7) "🎁" else "🪙", fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("+${day*100}", fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // Play Store Rewarded Ad booster simulation card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text("📺 SPONSORED BOOSTER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Watch Video, Earn Gems!", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Earn 5 Gems instantly by viewing a short ad sponsored by partners.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            viewModel.showRewardedAdToEarnGems {
                                viewModel.viewModelScope.launch {
                                    viewModel.adminSetResources(0, 5)
                                    viewModel.rewardAlertTitle = "Gems Received!"
                                    viewModel.spinResultRewardText = "Nice! You earned 5 Gems by watching the partner sponsored ad video."
                                    viewModel.rewardAlertCoins = 0
                                    viewModel.rewardAlertGems = 5
                                    viewModel.showRewardAlert = true
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("watch_video_ad_button")
                    ) {
                        Text("WATCH", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- 2. LEVELS PANEL ---
@Composable
fun LevelsPanel(
    progress: UserProgress,
    levelProgress: List<LevelProgress>,
    onPlayLevel: (Int) -> Unit
) {
    val completedLevelsMap = levelProgress.associateBy { it.levelId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🌌 LEVEL SELECTOR (1 - 750)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Display up to 100 levels sequentially for standard exploration (players scroll and unlock progressively)
            items(150) { index ->
                val levelId = index + 1
                val isCompleted = completedLevelsMap.containsKey(levelId)
                val starsEarned = completedLevelsMap[levelId]?.stars ?: 0
                
                // Progressive unlock rule: unlocked if level 1, or level L-1 is completed
                val isUnlocked = levelId == 1 || completedLevelsMap.containsKey(levelId - 1)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                !isUnlocked -> Color.DarkGray.copy(alpha = 0.3f)
                                isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .border(
                            width = 1.5.dp,
                            color = when {
                                !isUnlocked -> Color.Transparent
                                isCompleted -> MaterialTheme.colorScheme.primary
                                else -> Color.White.copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = isUnlocked) { onPlayLevel(levelId) }
                        .testTag("level_select_item_$levelId"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isUnlocked) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = levelId.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.White
                            )
                            if (isCompleted) {
                                Row {
                                    repeat(starsEarned) {
                                        Text("⭐", fontSize = 7.sp)
                                    }
                                }
                            } else {
                                Text(
                                    text = LevelGenerator.getCategoryForLevel(levelId).displayName.split(" ").first(),
                                    fontSize = 7.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 3. MISSIONS PANEL ---
@Composable
fun MissionsPanel(viewModel: GameViewModel) {
    val daily = viewModel.dailyMissions.collectAsState().value
    val weekly = viewModel.weeklyMissions.collectAsState().value
    val monthly = viewModel.monthlyMissions.collectAsState().value

    var subTab by remember { mutableStateOf(0) } // 0: Daily, 1: Weekly, 2: Monthly

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Daily", "Weekly", "Monthly").forEachIndexed { index, title ->
                val isSelected = subTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { subTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        val activeList = when (subTab) {
            1 -> weekly
            2 -> monthly
            else -> daily
        }

        if (activeList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading Missions...", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activeList) { mission ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(0.7f)) {
                                Text(
                                    text = mission.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val fraction = mission.currentValue.toFloat() / mission.targetValue.toFloat()
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${mission.currentValue} / ${mission.targetValue}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(0.3f)
                            ) {
                                Row(horizontalArrangement = Arrangement.Center) {
                                    Text("🪙${mission.coinsReward}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("💎${mission.gemsReward}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE040FB))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                when (mission.status) {
                                    0 -> {
                                        // In Progress
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Gray.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("IN PROGRESS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        }
                                    }
                                    1 -> {
                                        // Completed but unclaimed
                                        Button(
                                            onClick = { viewModel.claimMission(mission.missionId) },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .height(24.dp)
                                                .testTag("claim_mission_button_${mission.missionId}")
                                        ) {
                                            Text("CLAIM", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    2 -> {
                                        // Claimed
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Green.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("CLAIMED ✔", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 4. SHOP PANEL ---
@Composable
fun ShopPanel(progress: UserProgress, viewModel: GameViewModel) {
    var shopTab by remember { mutableStateOf(0) } // 0: Currencies, 1: Interface Themes, 2: VIP Packs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Currencies", "Themes", "VIP Pack").forEachIndexed { index, title ->
                val isSelected = shopTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { shopTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        when (shopTab) {
            0 -> {
                // Currencies storefront
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Coin Packs
                    item { Text("🪙 BUY COINS WITH GEMS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                    item {
                        ShopRowItem(
                            title = "Pouch of Coins",
                            rewardDesc = "500 Coins",
                            costDesc = "10 Gems",
                            icon = "🪙",
                            onBuy = { viewModel.buyCoinsPack(500, 10) }
                        )
                    }
                    item {
                        ShopRowItem(
                            title = "Chest of Coins",
                            rewardDesc = "2000 Coins",
                            costDesc = "35 Gems",
                            icon = "🪙",
                            onBuy = { viewModel.buyCoinsPack(2000, 35) }
                        )
                    }
                    item {
                        ShopRowItem(
                            title = "Nova Vault of Coins",
                            rewardDesc = "10000 Coins",
                            costDesc = "150 Gems",
                            icon = "🪙",
                            onBuy = { viewModel.buyCoinsPack(10000, 150) }
                        )
                    }

                    // Gem Packs
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { Text("💎 BUY GEMS WITH IN-APP BILLING (SIMULATED)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                    item {
                        ShopRowItem(
                            title = "Stack of Gems",
                            rewardDesc = "50 Gems",
                            costDesc = "$0.99 USD",
                            icon = "💎",
                            onBuy = { viewModel.buyGemsPack(50, 0.99) }
                        )
                    }
                    item {
                        ShopRowItem(
                            title = "Bucket of Gems",
                            rewardDesc = "300 Gems",
                            costDesc = "$4.99 USD",
                            icon = "💎",
                            onBuy = { viewModel.buyGemsPack(300, 4.99) }
                        )
                    }
                }
            }
            1 -> {
                // Dynamic Themes Customizer UI
                val unlockedThemes = progress.unlockedThemes.split(",").map { it.trim() }
                val currentTheme = progress.activeTheme

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { Text("🎨 CUSTOM INTERFACE BACKGROUNDS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                    
                    val themes = listOf(
                        Triple("Elegant Dark", "Premium deep night & indigo glow style", 0),
                        Triple("Neon Eclipse", "Vibrant midnight cyberpunk violet style", 500),
                        Triple("Emerald Forest", "Calm relaxing organic green style", 800),
                        Triple("Amber Glow", "Warm volcanic magma charcoal style", 1200),
                        Triple("Pastel Dreams", "Bright cheerful light color style", 1500)
                    )

                    items(themes) { theme ->
                        val themeName = theme.first
                        val isUnlocked = unlockedThemes.contains(themeName)
                        val isEquipped = currentTheme == themeName

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(themeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(theme.second, fontSize = 11.sp, color = Color.Gray)
                                }

                                when {
                                    isEquipped -> {
                                        Button(
                                            onClick = {},
                                            enabled = false,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("EQUIPPED", fontSize = 10.sp)
                                        }
                                    }
                                    isUnlocked -> {
                                        OutlinedButton(
                                            onClick = { viewModel.changeActiveTheme(themeName) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("EQUIP", fontSize = 10.sp)
                                        }
                                    }
                                    else -> {
                                        Button(
                                            onClick = { viewModel.unlockThemeFromShop(themeName, theme.third) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("🪙 ${theme.third}", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // VIP Membership Simulation
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👑", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("VIP NOVA MEMBER PACK", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unlock all themes immediately, earn double coins from completed puzzles, completely remove simulated advertisements, and receive +5,000 Coins + 200 Gems instantly!",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (progress.vipUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Green.copy(alpha = 0.2f))
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("VIP ACTIVE ✔", fontWeight = FontWeight.Bold, color = Color.Green)
                        }
                    } else {
                        GlowButton(
                            text = "ACTIVATE VIP ($9.99 USD)",
                            onClick = { viewModel.removeAdsVIPPremium() },
                            primaryColor = Color(0xFFFFD700),
                            accentColor = Color(0xFFFF9F00),
                            tag = "buy_vip_pack_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShopRowItem(
    title: String,
    rewardDesc: String,
    costDesc: String,
    icon: String,
    onBuy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(rewardDesc, fontSize = 11.sp, color = Color.Gray)
                }
            }

            Button(
                onClick = onBuy,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(costDesc, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 5. EXTRAS / LEADERBOARDS & ACHIEVEMENTS PANEL ---
@Composable
fun ExtrasPanel(progress: UserProgress, levelProgress: List<LevelProgress>, viewModel: GameViewModel) {
    var subTab by remember { mutableStateOf(0) } // 0: Lucky Spin, 1: Achievements, 2: Leaderboard

    val achievementsList = viewModel.achievements.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Lucky Spin", "Achievements", "Leaderboard").forEachIndexed { index, title ->
                val isSelected = subTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { subTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        when (subTab) {
            0 -> {
                // Spinning Wheel panel
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("🎰 LUCKY NOVA SPIN 🎰", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    
                    // Rotating spinning wheel graphics
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .rotate(viewModel.spinRotationAngle)
                            .border(6.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFFEF4444), Color(0xFF3B82F6),
                                        Color(0xFF10B981), Color(0xFFF59E0B),
                                        Color(0xFF8B5CF6), Color(0xFFEC4899),
                                        Color(0xFF06B6D4), Color(0xFFF97316),
                                        Color(0xFFEF4444)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing center node
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(2.dp, Color.Black, CircleShape)
                        )
                    }

                    // Needle pointer facing down
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Pin Needle",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .rotate(180f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { viewModel.spinLuckyWheel() },
                            enabled = !viewModel.isSpinning,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("lucky_spin_roll_button")
                        ) {
                            Text(if (viewModel.isSpinning) "ROLLING..." else "SPIN FOR 5 GEMS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            1 -> {
                // Achievements milestones list
                if (achievementsList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading achievements...", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(achievementsList) { ach ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(0.7f)) {
                                        Text(ach.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(ach.description, fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        // Target meter
                                        val percent = ach.currentValue.toFloat() / ach.targetValue.toFloat()
                                        LinearProgressIndicator(
                                            progress = { percent },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = MaterialTheme.colorScheme.secondary,
                                            trackColor = MaterialTheme.colorScheme.surface
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.weight(0.3f)
                                    ) {
                                        Text("🪙 +${ach.coinsReward}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFFB300))
                                        Spacer(modifier = Modifier.height(6.dp))

                                        when (ach.status) {
                                            0 -> {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.Gray.copy(alpha = 0.2f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("LOCKED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                }
                                            }
                                            1 -> {
                                                Button(
                                                    onClick = { viewModel.claimAchievement(ach.achievementId) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier
                                                        .height(24.dp)
                                                        .testTag("claim_achievement_button_${ach.achievementId}")
                                                ) {
                                                    Text("CLAIM", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            2 -> {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.Green.copy(alpha = 0.2f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("CLAIMED ✔", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Simulated Rivals ranking leaderboards
                val rivals = listOf(
                    Pair("NebulaKnight", 1500),
                    Pair("AstroCoder", 1250),
                    Pair("CosmicCat", 1100),
                    Pair("StarSeeker", 950),
                    Pair("You (Nova Hero)", progress.statsLevelsCompleted * 10),
                    Pair("QuantumBrain", 820),
                    Pair("GravityGuru", 600),
                    Pair("PuzzlerPluto", 450)
                ).sortedByDescending { it.second }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { Text("🏆 GLOBAL RIVALS REAL-TIME LEADERBOARD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                    
                    items(rivals.size) { index ->
                        val rival = rivals[index]
                        val isUser = rival.first.startsWith("You")

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                 else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${index + 1}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = if (index < 3) Color(0xFFFFD700) else Color.Gray,
                                        modifier = Modifier.width(36.dp)
                                    )
                                    Text(
                                        text = rival.first,
                                        fontWeight = if (isUser) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }

                                Text(
                                    text = "${rival.second} pts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 6. ADMIN CONTROL DASHBOARD ---
@Composable
fun AdminPanelPanel(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "🛠️ DEVELOPER CONTROL PANEL (PUZZLE NOVA)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Divider(color = Color.DarkGray)

        // Add Currencies
        Text("Resource Injection tools", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.adminSetResources(1000, 0) },
                modifier = Modifier.weight(1f).testTag("admin_add_coins")
            ) {
                Text("+1K Coins", fontSize = 11.sp)
            }
            Button(
                onClick = { viewModel.adminSetResources(0, 100) },
                modifier = Modifier.weight(1f).testTag("admin_add_gems")
            ) {
                Text("+100 Gems", fontSize = 11.sp)
            }
        }

        // Lock & Unlock selectors
        Text("Save Progress & Progression Overrides", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.adminUnlockFirstNLevels(30) },
                modifier = Modifier.weight(1f).testTag("admin_unlock_30_levels")
            ) {
                Text("Unlock 30 Lvl", fontSize = 11.sp)
            }
            Button(
                onClick = { viewModel.adminUnlockFirstNLevels(100) },
                modifier = Modifier.weight(1f).testTag("admin_unlock_100_levels")
            ) {
                Text("Unlock 100 Lvl", fontSize = 11.sp)
            }
        }

        // Reset Everything
        Button(
            onClick = { viewModel.adminResetAllProgress() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().testTag("admin_reset_progress")
        ) {
            Text("RESET SYSTEM (WIPE SAVE)", fontWeight = FontWeight.Bold)
        }

        // Ads Sandbox testers
        Text("Ad Sandbox Testing Tools", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.showSimulatedInterstitialAd = true },
                modifier = Modifier.weight(1f).testTag("admin_test_interstitial_ad")
            ) {
                Text("Interstitial Ad", fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = {
                    viewModel.showRewardedAdToEarnGems {
                        viewModel.viewModelScope.launch {
                            viewModel.adminSetResources(0, 10)
                            viewModel.rewardAlertTitle = "Mock Ad Completed!"
                            viewModel.spinResultRewardText = "Ad verification successful in sandbox. +10 Gems delivered!"
                            viewModel.rewardAlertCoins = 0
                            viewModel.rewardAlertGems = 10
                            viewModel.showRewardAlert = true
                        }
                    }
                },
                modifier = Modifier.weight(1f).testTag("admin_test_rewarded_ad")
            ) {
                Text("Rewarded Ad", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    tag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(tag)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color(0xFF818CF8) else Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color(0xFF818CF8) else Color(0xFF94A3B8)
        )
    }
}

// =================== 23 GAME MODES SCREEN ===================

data class GameModeItem(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val objective: String,
    val rewards: String,
    val unlockLevel: Int,
    val color: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameModesScreen(
    viewModel: GameViewModel,
    progress: UserProgress,
    onPlayMode: (String) -> Unit,
    onBack: () -> Unit
) {
    val modeProgressList = viewModel.allGameModeProgress.collectAsState().value
    val progressMap = modeProgressList.associateBy { it.modeId }
    
    var selectedMode by remember { mutableStateOf<GameModeItem?>(null) }
    var showMultiplayerLobby by remember { mutableStateOf(false) }
    
    val gameModes = remember {
        listOf(
            GameModeItem("kids", "Kids Mode", "👶", "Very easy puzzles with colorful visuals and unlimited moves. Perfect for young thinkers.", "Solve extra-simple grids with zero constraints.", "15 Coins, 1 Gem", 1, Color(0xFFFB7185)),
            GameModeItem("beginner", "Beginner Mode", "🟢", "A welcoming space for newcomers. Easy puzzles to master the basics of each logic style.", "Complete standard easy difficulty levels.", "25 Coins, 1 Gem", 1, Color(0xFF4ADE80)),
            GameModeItem("normal", "Normal Mode", "🔵", "The standard Puzzle Nova experience. Balanced logic puzzles with classic limitations.", "Complete levels with standard limits.", "45 Coins, 2 Gems", 1, Color(0xFF60A5FA)),
            GameModeItem("endless", "Endless Mode", "♾️", "Unlimited, infinite puzzles. Scale difficulty indefinitely and build massive streaks.", "Keep solving endless puzzles without stopping.", "35 Coins + streak multipliers", 2, Color(0xFFFBBF24)),
            GameModeItem("quick_play", "Quick Play Mode", "⚡", "Jump straight into a single random puzzle with boosted completion rewards.", "Solve a single randomized high-pace puzzle.", "60 Coins, 3 Gems", 2, Color(0xFFF472B6)),
            GameModeItem("relax", "Relax Mode", "🧘", "Zero timers, zero move limits, and zero pressure. Enjoy solving at your own leisure.", "Untimed puzzle solving without failure risks.", "20 Coins, 0 Gems", 3, Color(0xFF2DD4BF)),
            GameModeItem("practice", "Practice Mode", "🛠️", "Hone your puzzle techniques in any category with infinite retries and no penalties.", "Configure custom grids and test strategy.", "5 Coins, 0 Gems (unlimited play)", 3, Color(0xFF94A3B8)),
            GameModeItem("random", "Random Mode", "🎲", "Total mystery. Randomized dimensions, colors, and puzzle logic every single time.", "Solve a completely randomized grid setup.", "55 Coins, 3 Gems", 4, Color(0xFFA78BFA)),
            GameModeItem("memory", "Memory Mode", "🃏", "Challenge your spatial recall. Exclusively generates Memory Match with expanding grids.", "Match all card pairs within moves constraints.", "50 Coins, 2 Gems", 4, Color(0xFFF43F5E)),
            GameModeItem("logic", "Logic Mode", "🧪", "Pure analytical sorting. Exclusively alternates between Water Sort and Pipe Connect.", "Apply fluid math and rotation pathways.", "50 Coins, 2 Gems", 5, Color(0xFF06B6D4)),
            GameModeItem("pattern", "Pattern Mode", "🧩", "Enhance visual recognition. Alternates between Slide Puzzle and Spot Differences.", "Spot anomalies or slide blocks to solve.", "50 Coins, 2 Gems", 5, Color(0xFF84CC16)),
            GameModeItem("daily_challenge", "Daily Challenge", "📅", "A unique, hand-crafted master puzzle that resets daily. Massive reward packages!", "Solve today's cosmic puzzle node.", "150 Coins, 10 Gems", 6, Color(0xFF3B82F6)),
            GameModeItem("weekly_challenge", "Weekly Trial", "🏆", "An extra-large, ultra-complex boss grid that resets every week. Can you conquer it?", "Solve the weekly mega-scale puzzle grid.", "350 Coins, 25 Gems", 6, Color(0xFFEAB308)),
            GameModeItem("hard", "Hard Mode", "🟠", "Complex grid dimensions and tighter limit margins to test your logical boundaries.", "Solve high-complexity puzzles.", "75 Coins, 4 Gems", 7, Color(0xFFFB923C)),
            GameModeItem("expert", "Expert Mode", "🔴", "Virtually zero margin for error. Brutal move counts and 45-second timers.", "Conquerexpert-level grids under pressure.", "120 Coins, 8 Gems", 8, Color(0xFFEF4444)),
            GameModeItem("time_attack", "Time Attack", "⏱️", "Race against an active countdown! Correct placements add 5 seconds to your clock.", "Keep the clock alive and solve before 0s.", "80 Coins, 5 Gems", 9, Color(0xFF14B8A6)),
            GameModeItem("survival", "Survival Mode", "❤️", "Start with 3 lives. Failures cost lives. Clear as many consecutive puzzles as you can!", "Survive with your hearts intact.", "110 Coins + 5 Gems per clear", 10, Color(0xFFEC4899)),
            GameModeItem("brain_training", "Brain Training", "🧠", "A cognitive routine comprising 3 sequential puzzles. Boost your daily IQ Index!", "Complete the daily 3-puzzle logic routine.", "100 Coins, 6 Gems", 11, Color(0xFF8B5CF6)),
            GameModeItem("iq_challenge", "IQ Challenge", "💡", "Devious spatial-analytica grids crafted to push logical reasoning to the absolute peak.", "Verify high cognitive spatial problems.", "200 Coins, 12 Gems", 12, Color(0xFFFBBF24)),
            GameModeItem("master", "Master Mode", "👑", "The peak of offline difficulty. Huge grids, zero hints allowed, and 30-second timers.", "Conquer master-level grids.", "200 Coins, 12 Gems", 13, Color(0xFFD946EF)),
            GameModeItem("tournament", "Tournament", "🏁", "Compete speedrun style in visual brackets against 7 simulated AI puzzle masters.", "Climb speedrun brackets to win gold.", "300 Coins, 15 Gems", 14, Color(0xFFF97316)),
            GameModeItem("seasonal_events", "Seasonal Event", "🌌", "Cosmic theme festivals like 'Nebula Eclipse' featuring themed aesthetics and badges.", "Complete exclusive galaxy event grids.", "180 Coins, 12 Gems", 15, Color(0xFF6366F1)),
            GameModeItem("multiplayer", "Multiplayer", "👥", "Co-op and head-to-head live online matches. Battle puzzle masters across the globe!", "Defeat opponents in real-time speed solves.", "Rank Points, Double Gems", 999, Color(0xFF64748B))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090514))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1E1B4B))
                            .testTag("modes_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "GAME CHAMBER",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "23 SPECIALIZED PUZZLE MODES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF818CF8),
                            letterSpacing = 2.sp
                        )
                    }
                }

                // Players online tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF22C55E).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "24.8K ONLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
            }

            // Description intro
            Text(
                text = "Each mode runs its own levels, progress tracker, unlock requirements, and coin/gem rewards. Tap any chamber node below to preview objective specs.",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid layout of all 23 modes
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(gameModes.size) { index ->
                    val mode = gameModes[index]
                    val isUnlocked = progress.level >= mode.unlockLevel || mode.unlockLevel == 1
                    val modeProgress = progressMap[mode.id]
                    val completedLevel = modeProgress?.currentLevel?.minus(1) ?: 0
                    
                    val cardBorder = if (isUnlocked) {
                        BorderStroke(1.dp, mode.color.copy(alpha = 0.4f))
                    } else {
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) Color(0xFF16132A) else Color(0xFF0F0B1E)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = cardBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .clickable {
                                if (mode.id == "multiplayer") {
                                    showMultiplayerLobby = true
                                } else {
                                    selectedMode = mode
                                }
                            }
                            .testTag("mode_card_${mode.id}")
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Circular Icon with mode color background
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(mode.color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = mode.icon, fontSize = 18.sp)
                                    }

                                    if (!isUnlocked) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Red.copy(alpha = 0.1f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "🔒 LV. ${mode.unlockLevel}",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF43F5E)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF22C55E).copy(alpha = 0.1f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF22C55E)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = mode.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = if (isUnlocked) Color.White else Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                if (isUnlocked) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (mode.id == "tournament") "Speedrun Tourney" else "Clear: $completedLevel Puzzles",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.LightGray.copy(alpha = 0.7f)
                                        )
                                        if (modeProgress != null && modeProgress.starsEarned > 0) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "★${modeProgress.starsEarned}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFFFB300)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Increase level to unlock",
                                        fontSize = 9.sp,
                                        color = Color.Gray.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mode Details Dialog Popup
        selectedMode?.let { mode ->
            val isUnlocked = progress.level >= mode.unlockLevel || mode.unlockLevel == 1
            val modeProgress = progressMap[mode.id]
            val completedLevel = modeProgress?.currentLevel?.minus(1) ?: 0
            val currentLvl = modeProgress?.currentLevel ?: 1

            AlertDialog(
                onDismissRequest = { selectedMode = null },
                containerColor = Color(0xFF131024),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 6.dp,
                title = null,
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Big emoji with glowing ring
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(mode.color.copy(alpha = 0.15f))
                                .border(1.5.dp, mode.color.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = mode.icon, fontSize = 36.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = mode.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = mode.description,
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Specs Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0917)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🎯 Objective", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Text("🎁 Rewards Matrix", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = mode.objective,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.weight(0.5f)
                                    )
                                    Text(
                                        text = mode.rewards,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF22C55E),
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.5f)
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("📈 Mode Progress", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Text(
                                        text = if (isUnlocked) "Level $currentLvl" else "Locked",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = mode.color
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Completed: $completedLevel Puzzles • High Score: ${modeProgress?.highestScore ?: 0} PTS",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        // Independent progress wipe trigger
                        if (isUnlocked && completedLevel > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Reset Mode Progress",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.viewModelScope.launch {
                                            val fresh = GameModeProgress(modeId = mode.id, currentLevel = 1)
                                            viewModel.repository.saveGameModeProgress(fresh)
                                            selectedMode = null
                                        }
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (isUnlocked) {
                            Button(
                                onClick = {
                                    onPlayMode(mode.id)
                                    selectedMode = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = mode.color),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mode_start_button_${mode.id}")
                            ) {
                                Text("START LEVEL $currentLvl ➔", fontWeight = FontWeight.Black)
                            }
                        } else {
                            Button(
                                onClick = { },
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🔒 LOCKED (REACH LEVEL ${mode.unlockLevel})", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )
        }

        // Multiplayer Chamber Coming Soon Overlay Lobby Simulator
        if (showMultiplayerLobby) {
            AlertDialog(
                onDismissRequest = { showMultiplayerLobby = false },
                containerColor = Color(0xFF0F0B1E),
                shape = RoundedCornerShape(20.dp),
                title = null,
                text = {
                    var simulatedStep by remember { mutableStateOf(0) }
                    LaunchedEffect(Unit) {
                        while (simulatedStep < 3) {
                            delay(1000)
                            simulatedStep++
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "👥 MULTIPLAYER CHAMBER",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "CO-OP & LIVE OPPONENT DUELS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD946EF),
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, Color(0xFFD946EF).copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when (simulatedStep) {
                                    0 -> {
                                        CircularProgressIndicator(color = Color(0xFFD946EF), modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Initializing Peer-to-Peer Hub...", fontSize = 11.sp, color = Color.LightGray)
                                    }
                                    1 -> {
                                        CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Contacting Nova-Node Relay Servers...", fontSize = 11.sp, color = Color.LightGray)
                                    }
                                    2 -> {
                                        CircularProgressIndicator(color = Color(0xFF22C55E), modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Lobby Node Created! Synchronizing ping...", fontSize = 11.sp, color = Color.LightGray)
                                    }
                                    else -> {
                                        Text("📡 MULTIPLAYER READY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Verifying local sandbox nodes... Connected!", fontSize = 10.sp, color = Color.Gray)
                                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.05f))
                                        
                                        // Display live bracket players simulation
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Local Player (JD)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                            Text("PING: 14MS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Gray))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Opponent Spot (Searching)", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Text("CONNECTING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Multiplayer Duels will allow you to race live opponents in solving identical logic grids, earning Rank points and exclusive cosmic cosmetics.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Coming Soon in Puzzle Nova 2.0!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD946EF),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showMultiplayerLobby = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B4B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLOSE HUB", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

// --- PREMIUM COSMIC SETTINGS SCREEN WITH OFFICIAL LOGO ---
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    progress: UserProgress,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Row with Back Button and Panel Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "SYSTEM CONFIGURATION",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center: Official Puzzle Nova Logo (Original Design and Colors) with Subtle Cosmic Glow
        Box(
            modifier = Modifier
                .size(200.dp)
                .testTag("settings_logo_area"),
            contentAlignment = Alignment.Center
        ) {
            // Subtle neon glow halo behind the logo card
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFD946EF).copy(alpha = 0.25f),
                                Color(0xFF818CF8).copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f)), // Golden frame
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.size(150.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.puzzle_nova_banner),
                    contentDescription = "Puzzle Nova Official Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings Category Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B26)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AUDIO & IMMERSION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF818CF8),
                    letterSpacing = 1.sp
                )

                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🔊", fontSize = 18.sp)
                        Column {
                            Text("Sound FX", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Ui clicks & puzzle feedback", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = progress.soundEnabled,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.updateSettings(
                                    sound = it,
                                    music = progress.musicEnabled,
                                    vibration = progress.vibrationEnabled,
                                    kidsMode = progress.kidsModeEnabled
                                )
                            }
                        },
                        modifier = Modifier.testTag("toggle_sound")
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.05f))

                // Music Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🎵", fontSize = 18.sp)
                        Column {
                            Text("Music Loops", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Ambient cosmic soundscapes", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = progress.musicEnabled,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.updateSettings(
                                    sound = progress.soundEnabled,
                                    music = it,
                                    vibration = progress.vibrationEnabled,
                                    kidsMode = progress.kidsModeEnabled
                                )
                            }
                        },
                        modifier = Modifier.testTag("toggle_music")
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.05f))

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("📳", fontSize = 18.sp)
                        Column {
                            Text("Haptic Vibration", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Tactile grid collision response", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = progress.vibrationEnabled,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.updateSettings(
                                    sound = progress.soundEnabled,
                                    music = progress.musicEnabled,
                                    vibration = it,
                                    kidsMode = progress.kidsModeEnabled
                                )
                            }
                        },
                        modifier = Modifier.testTag("toggle_vibration")
                    )
                }
            }
        }

        // Gameplay Modes Settings Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B26)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ACCESSIBILITY & MODES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF818CF8),
                    letterSpacing = 1.sp
                )

                // Kids Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🧸", fontSize = 18.sp)
                        Column {
                            Text("Kids Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Infinite moves, gentle hints, simplified grids", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = progress.kidsModeEnabled,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.updateSettings(
                                    sound = progress.soundEnabled,
                                    music = progress.musicEnabled,
                                    vibration = progress.vibrationEnabled,
                                    kidsMode = it
                                )
                            }
                        },
                        modifier = Modifier.testTag("toggle_kids_mode")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Puzzle Nova v1.8.0 - Cosmic Edition",
            fontSize = 10.sp,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
    }
}
