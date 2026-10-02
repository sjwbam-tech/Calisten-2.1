package com.example.ui.parallax

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.IceBlueGlow
import com.example.ui.theme.Obsidian950

/**
 * Reusable Cinematic Parallax Hero Header.
 *
 * Implements GPU-accelerated layered depth:
 * 1. Background image (slow translation, gentle scale)
 * 2. Atmospheric vignette (ice-blue luminescent glow + obsidian gradient)
 * 3. Foreground content container (text, badges, action buttons)
 */
@Composable
fun CinematicParallaxHero(
    @DrawableRes backgroundImageRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    height: Dp = 240.dp,
    scrollState: ScrollState? = null,
    lazyListState: LazyListState? = null,
    config: ParallaxConfig = ParallaxConfig.LibraryHeader,
    reduceMotion: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val parallaxModifier = when {
        scrollState != null -> Modifier.parallaxScroll(scrollState, config, reduceMotion)
        lazyListState != null -> Modifier.parallaxLazyScroll(lazyListState, config, reduceMotion)
        else -> Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // Layer 1: Background Image with Parallax
        Image(
            painter = painterResource(id = backgroundImageRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(parallaxModifier)
        )

        // Layer 2: Cinematic Atmosphere & Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Obsidian950.copy(alpha = 0.5f),
                            Color.Transparent,
                            Obsidian950.copy(alpha = 0.85f),
                            Obsidian950
                        )
                    )
                )
        )

        // Subtle Ice-Blue Radial Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            IceBlueGlow.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Layer 3: Foreground Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            content()
        }
    }
}
