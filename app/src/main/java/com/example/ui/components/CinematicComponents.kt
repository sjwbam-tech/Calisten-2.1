package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceBlueGlow
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.Obsidian700
import com.example.ui.theme.Obsidian800
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950

/**
 * Editorial typography component for monumental numbers (e.g., Week "08", 75%, 26)
 */
@Composable
fun EditorialNumber(
    number: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 64.sp,
    color: Color = FrostWhite,
    subLabel: String? = null,
    subColor: Color = IceBluePrimary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = color,
            lineHeight = fontSize * 0.95f,
            letterSpacing = (-1.5).sp,
            textAlign = TextAlign.Center
        )
        if (!subLabel.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = subColor,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Translucent glass panel with refined borders and optional ice-blue highlight
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Obsidian900.copy(alpha = 0.72f),
    borderColor: Color = GlassCardBorderSubtle,
    cornerRadius: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
    ) {
        content()
    }
}

/**
 * Cinematic Parallax Layer that transforms scroll offset to translation/scale on GPU
 */
@Composable
fun ParallaxLayer(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    scrollProgress: Float, // 0.0f to 1.0f
    speedMultiplier: Float = 0.5f,
    scaleRange: ClosedFloatingPointRange<Float> = 1.0f..1.15f,
    alphaRange: ClosedFloatingPointRange<Float> = 1.0f..0.2f,
    reduceMotion: Boolean = false
) {
    val effectiveProgress = scrollProgress.coerceIn(0f, 1f)
    val translationY = if (reduceMotion) 0f else -effectiveProgress * 250f * speedMultiplier
    val scale = if (reduceMotion) 1.0f else scaleRange.start + (scaleRange.endInclusive - scaleRange.start) * effectiveProgress
    val alpha = alphaRange.start + (alphaRange.endInclusive - alphaRange.start) * effectiveProgress

    Image(
        painter = painter,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                this.translationY = translationY
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha.coerceIn(0f, 1f)
            }
    )
}

/**
 * Atmospheric overlay providing depth, gradient shadows and ice-blue vignettes
 */
@Composable
fun AtmosphereLayer(
    modifier: Modifier = Modifier,
    vignetteIntensity: Float = 0.8f,
    iceGlowIntensity: Float = 0.25f
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Obsidian950.copy(alpha = 0.85f * vignetteIntensity),
                        Color.Transparent,
                        Obsidian950.copy(alpha = 0.4f * vignetteIntensity),
                        Obsidian950.copy(alpha = 0.95f * vignetteIntensity),
                        Obsidian950
                    )
                )
            )
    )
    if (iceGlowIntensity > 0.05f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            IceBlueGlow.copy(alpha = 0.18f * iceGlowIntensity),
                            Color.Transparent
                        ),
                        center = Offset(200f, 400f),
                        radius = 800f
                    )
                )
        )
    }
}

/**
 * Scene Indicator for cinematic journey (01, 02, 03...)
 */
@Composable
fun SceneIndicator(
    currentSceneIndex: Int,
    totalScenes: Int,
    sceneTitlesFa: List<String>,
    modifier: Modifier = Modifier,
    onSceneClick: (Int) -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(Obsidian950.copy(alpha = 0.65f))
            .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(30.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until totalScenes) {
            val isCurrent = i == currentSceneIndex
            val dotWidth by animateDpAsState(
                targetValue = if (isCurrent) 22.dp else 6.dp,
                animationSpec = tween(300),
                label = "indicator_width"
            )
            val dotColor = if (isCurrent) IceBluePrimary else DarkSlate

            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(dotWidth)
                    .clip(RoundedCornerShape(3.dp))
                    .background(dotColor)
                    .clickable { onSceneClick(i) }
            )
        }
        val label = sceneTitlesFa.getOrNull(currentSceneIndex) ?: ""
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = FrostWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Luxury Floating Glass Navigation Bar (Full App Navigation)
 * Provides fluid Persian RTL navigation with direct access to:
 * Home, Programs, Exercises, Skills, Progress, Plus Expandable Sheet for Nutrition, Recovery, Science, Profiles, Settings
 */
@Composable
fun FloatingGlassMenu(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onOpenTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMoreMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // More drawer for secondary modules
        AnimatedVisibility(
            visible = expandedMoreMenu,
            enter = fadeIn(tween(200)) + slideInVertically(tween(250)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { it / 2 }
        ) {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                backgroundColor = Obsidian900.copy(alpha = 0.94f),
                borderColor = GlassCardBorder,
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بخش‌های تخصصی کالیستن",
                            style = MaterialTheme.typography.titleSmall,
                            color = IceBlueLight,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { expandedMoreMenu = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = MutedSlate
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SecondaryMenuItem(
                            icon = Icons.Default.LocalDining,
                            label = "تغذیه",
                            selected = currentScreen == "nutrition",
                            onClick = {
                                expandedMoreMenu = false
                                onNavigate("nutrition")
                            }
                        )
                        SecondaryMenuItem(
                            icon = Icons.Default.Nightlight,
                            label = "ریکاوری",
                            selected = currentScreen == "recovery",
                            onClick = {
                                expandedMoreMenu = false
                                onNavigate("recovery")
                            }
                        )
                        SecondaryMenuItem(
                            icon = Icons.Default.Science,
                            label = "شواهد علمی",
                            selected = currentScreen == "science",
                            onClick = {
                                expandedMoreMenu = false
                                onNavigate("science")
                            }
                        )
                        SecondaryMenuItem(
                            icon = Icons.Default.Person,
                            label = "پروفایل‌ها",
                            selected = currentScreen == "profiles",
                            onClick = {
                                expandedMoreMenu = false
                                onNavigate("profiles")
                            }
                        )
                        SecondaryMenuItem(
                            icon = Icons.Default.Settings,
                            label = "تنظیمات",
                            selected = currentScreen == "settings",
                            onClick = {
                                expandedMoreMenu = false
                                onNavigate("settings")
                            }
                        )
                    }
                }
            }
        }

        // Primary Floating Pill
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Obsidian900.copy(alpha = 0.85f),
            shadowElevation = 12.dp,
            modifier = Modifier
                .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(32.dp))
                .testTag("floating_glass_menu")
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FloatingPillItem(
                    icon = Icons.Default.Home,
                    label = "خانه",
                    selected = currentScreen == "dashboard",
                    onClick = {
                        expandedMoreMenu = false
                        onNavigate("dashboard")
                    }
                )
                FloatingPillItem(
                    icon = Icons.Default.FitnessCenter,
                    label = "برنامه",
                    selected = currentScreen == "programs",
                    onClick = {
                        expandedMoreMenu = false
                        onNavigate("programs")
                    }
                )
                FloatingPillItem(
                    icon = Icons.Default.MenuBook,
                    label = "حرکات",
                    selected = currentScreen == "exercises",
                    onClick = {
                        expandedMoreMenu = false
                        onNavigate("exercises")
                    }
                )
                FloatingPillItem(
                    icon = Icons.Default.Star,
                    label = "مهارت",
                    selected = currentScreen == "skills",
                    onClick = {
                        expandedMoreMenu = false
                        onNavigate("skills")
                    }
                )
                FloatingPillItem(
                    icon = Icons.Default.AutoGraph,
                    label = "پیشرفت",
                    selected = currentScreen == "progress",
                    onClick = {
                        expandedMoreMenu = false
                        onNavigate("progress")
                    }
                )

                // Separator
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(DarkSlate.copy(alpha = 0.5f))
                )

                // Timer Shortcut
                IconButton(
                    onClick = onOpenTimer,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(IceBlueContainer.copy(alpha = 0.6f))
                        .testTag("floating_timer_shortcut")
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "کرنومتر و تایمر",
                        tint = IceBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // More Menu Button
                IconButton(
                    onClick = { expandedMoreMenu = !expandedMoreMenu },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (expandedMoreMenu) Obsidian700 else Color.Transparent)
                        .testTag("floating_more_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "سایر بخش‌ها",
                        tint = if (expandedMoreMenu) IceBluePrimary else MutedSlate,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingPillItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) IceBluePrimary.copy(alpha = 0.16f) else Color.Transparent
    val tint = if (selected) IceBluePrimary else MutedSlate
    val textColor = if (selected) FrostWhite else MutedSlate

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        if (selected) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun SecondaryMenuItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (selected) IceBluePrimary.copy(alpha = 0.25f) else Obsidian800)
                .border(
                    1.dp,
                    if (selected) IceBluePrimary else GlassCardBorderSubtle,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) IceBluePrimary else FrostWhite,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) IceBlueLight else MutedSlate,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
