package com.example.ui.parallax

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * High-performance scroll parallax modifier that operates strictly during the Draw phase
 * via graphicsLayer lambda, completely avoiding Compose recomposition cycles on every scroll event.
 */
fun Modifier.parallaxScroll(
    scrollState: ScrollState,
    config: ParallaxConfig = ParallaxConfig.SubtleSection,
    isReducedMotion: Boolean = false,
    maxScrollPx: Float = 600f
): Modifier = this.graphicsLayer {
    if (config.reduceMotion || isReducedMotion) {
        translationY = 0f
        scaleX = 1f
        scaleY = 1f
        alpha = 1f
        return@graphicsLayer
    }

    val fraction = (scrollState.value.toFloat() / maxScrollPx).coerceIn(0f, 1f)
    translationY = config.computeTranslationY(fraction, isReducedMotion)
    val scale = config.computeScale(fraction, isReducedMotion)
    scaleX = scale
    scaleY = scale
    alpha = config.computeAlpha(fraction, isReducedMotion)
}

/**
 * LazyList parallax modifier for lazy scroll containers (Exercise Library, Skills, History).
 */
fun Modifier.parallaxLazyScroll(
    lazyListState: LazyListState,
    config: ParallaxConfig = ParallaxConfig.SubtleSection,
    isReducedMotion: Boolean = false,
    maxScrollPx: Float = 500f
): Modifier = this.graphicsLayer {
    if (config.reduceMotion || isReducedMotion) {
        translationY = 0f
        scaleX = 1f
        scaleY = 1f
        alpha = 1f
        return@graphicsLayer
    }

    val firstVisible = lazyListState.firstVisibleItemIndex
    val firstOffset = lazyListState.firstVisibleItemScrollOffset
    val totalOffset = (firstVisible * 200f) + firstOffset
    val fraction = (totalOffset / maxScrollPx).coerceIn(0f, 1f)

    translationY = config.computeTranslationY(fraction, isReducedMotion)
    val scale = config.computeScale(fraction, isReducedMotion)
    scaleX = scale
    scaleY = scale
    alpha = config.computeAlpha(fraction, isReducedMotion)
}

/**
 * Subtle 3D depth and slight elevation translation for cards.
 */
fun Modifier.parallaxCardDepth(
    scrollState: ScrollState,
    config: ParallaxConfig = ParallaxConfig.SubtleSection,
    isReducedMotion: Boolean = false,
    itemOffsetPx: Float = 300f
): Modifier = this.graphicsLayer {
    if (config.reduceMotion || isReducedMotion || !config.enableDepth) {
        translationY = 0f
        scaleX = 1f
        scaleY = 1f
        return@graphicsLayer
    }

    val relativeScroll = (scrollState.value - itemOffsetPx).coerceIn(-200f, 200f)
    val tilt = (relativeScroll / 200f) * 6f * config.intensity
    translationY = -tilt
    shadowElevation = (8f + (kotlin.math.abs(relativeScroll) / 50f)).coerceIn(4f, 16f)
}

/**
 * Reveals an element smoothly as user scrolls towards it.
 */
fun Modifier.parallaxReveal(
    scrollState: ScrollState,
    targetOffsetPx: Float,
    config: ParallaxConfig = ParallaxConfig.SubtleSection,
    isReducedMotion: Boolean = false
): Modifier = this.graphicsLayer {
    if (config.reduceMotion || isReducedMotion) {
        alpha = 1f
        translationY = 0f
        return@graphicsLayer
    }

    val currentScroll = scrollState.value.toFloat()
    val distance = targetOffsetPx - currentScroll
    val revealProgress = (1f - (distance / 300f)).coerceIn(0f, 1f)

    alpha = if (revealProgress < config.revealThreshold) 0.3f + (revealProgress * 0.7f) else 1f
    translationY = (1f - revealProgress) * 20f * config.intensity
}
