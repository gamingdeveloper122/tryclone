package com.example.game.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ThemeColors
import kotlinx.coroutines.delay

// --- Dynamic Glowing Buttons ---
@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.secondary,
    tag: String = ""
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val buttonModifier = if (enabled) {
        modifier
            .scale(pulseScale)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(primaryColor, accentColor)))
            .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    } else {
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Gray.copy(alpha = 0.3f))
            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    }

    Box(
        modifier = buttonModifier
            .testTag(if (tag.isNotEmpty()) tag else "glow_button_${text.lowercase().replace(" ", "_")}")
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

// --- Star Rating Display ---
@Composable
fun StarRatingDisplay(
    stars: Int,
    maxStars: Int = 3,
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val filled = i <= stars
            val scaleSpec = remember { Animatable(0f) }
            
            LaunchedEffect(stars) {
                delay(i * 100L)
                scaleSpec.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))
            }

            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Star $i",
                tint = if (filled) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(size.dp)
                    .scale(scaleSpec.value)
                    .padding(2.dp)
            )
        }
    }
}

// --- Card Header Section ---
@Composable
fun CardSectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// --- Decorative Info Row ---
@Composable
fun InfoBadge(
    label: String,
    value: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label: ",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- Common Reward Alert Dialog ---
@Composable
fun RewardDialog(
    title: String,
    subtitle: String,
    coins: Int,
    gems: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Celebration Star Animation
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Text(
                    text = "🎉",
                    fontSize = 52.sp,
                    modifier = Modifier.scale(scale)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Rewards Row
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (coins > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🪙", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+$coins Coins",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB300)
                            )
                        }
                    }
                    if (gems > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💎", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+$gems Gems",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE040FB)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reward_dismiss_button")
                ) {
                    Text("CLAIM ALL", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Simulated Ad Container Dialog (For interstitial or rewarded ads test) ---
@Composable
fun SimulatedAdDialog(
    adUnitType: String, // "Interstitial" or "Rewarded Video"
    onDismiss: (completed: Boolean) -> Unit
) {
    var timerCount by remember { mutableStateOf(if (adUnitType == "Interstitial") 3 else 8) }
    
    LaunchedEffect(Unit) {
        while (timerCount > 0) {
            delay(1000)
            timerCount--
        }
    }

    Dialog(onDismissRequest = { if (timerCount == 0) onDismiss(true) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("SPONSORED AD", fontSize = 9.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    }
                    
                    if (timerCount > 0) {
                        Text("Skip in ${timerCount}s", fontSize = 12.sp, color = Color.White)
                    } else {
                        Box(
                            modifier = Modifier
                                .background(Color.White, CircleShape)
                                .clickable { onDismiss(true) }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("CLOSE ✕", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Ad Content Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌌", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "PUZZLE NOVA PREMIUM",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Offline Secured Brain Play. Get VIP to skip ads forever!",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (adUnitType == "Rewarded Video") "Watch to complete: rewards delivered after timer."
                           else "Ad closes automatically or tap close button.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
