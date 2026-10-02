package com.example.ui.parallax

import androidx.compose.runtime.compositionLocalOf

/**
 * Global composition local for accessing the user's reduced-motion preference.
 */
val LocalReduceMotion = compositionLocalOf { false }

/**
 * Reusable configuration defining parallax physics, translation limits, and depth curves.
 */
data class ParallaxConfig(
    val intensity: Float = 1.0f,
    val maxTranslationY: Float = 120f,
    val scaleRange: ClosedFloatingPointRange<Float> = 1.0f..1.08f,
    val alphaRange: ClosedFloatingPointRange<Float> = 1.0f..0.2f,
    val layerSpeed: Float = 0.5f,
    val revealThreshold: Float = 0.15f,
    val enableDepth: Boolean = true,
    val reduceMotion: Boolean = false
) {
    /**
     * Resolves effective translation based on current scroll offset and motion preferences.
     */
    fun computeTranslationY(fraction: Float, isReducedMotion: Boolean): Float {
        if (reduceMotion || isReducedMotion) return 0f
        return -fraction * maxTranslationY * layerSpeed * intensity
    }

    /**
     * Resolves effective scale based on current scroll offset and motion preferences.
     */
    fun computeScale(fraction: Float, isReducedMotion: Boolean): Float {
        if (reduceMotion || isReducedMotion) return 1.0f
        val rangeDelta = scaleRange.endInclusive - scaleRange.start
        return scaleRange.start + (rangeDelta * fraction * intensity).coerceIn(0f, rangeDelta)
    }

    /**
     * Resolves effective alpha based on current scroll offset and motion preferences.
     */
    fun computeAlpha(fraction: Float, isReducedMotion: Boolean): Float {
        if (reduceMotion || isReducedMotion) return 1.0f
        val rangeDelta = alphaRange.endInclusive - alphaRange.start
        return (alphaRange.start + (rangeDelta * fraction * intensity)).coerceIn(0f, 1f)
    }

    companion object {
        /**
         * Strongest cinematic depth for Dashboard / Hero section.
         */
        val HeroCinematic = ParallaxConfig(
            intensity = 1.0f,
            maxTranslationY = 140f,
            scaleRange = 1.0f..1.12f,
            alphaRange = 1.0f..0.25f,
            layerSpeed = 0.6f
        )

        /**
         * Athletic, dramatic depth for Calisthenics Skills progression.
         */
        val SkillsCinematic = ParallaxConfig(
            intensity = 0.85f,
            maxTranslationY = 100f,
            scaleRange = 1.0f..1.08f,
            alphaRange = 1.0f..0.35f,
            layerSpeed = 0.5f
        )

        /**
         * Controlled, elegant depth for Exercise Library headers and filter bars.
         */
        val LibraryHeader = ParallaxConfig(
            intensity = 0.65f,
            maxTranslationY = 70f,
            scaleRange = 1.0f..1.05f,
            alphaRange = 1.0f..0.45f,
            layerSpeed = 0.4f
        )

        /**
         * Focused technique hero depth for Exercise Details.
         */
        val DetailHero = ParallaxConfig(
            intensity = 0.75f,
            maxTranslationY = 90f,
            scaleRange = 1.0f..1.07f,
            alphaRange = 1.0f..0.30f,
            layerSpeed = 0.45f
        )

        /**
         * Subtle, stable depth for Program Builder to keep configuration inputs steady.
         */
        val BuilderSubtle = ParallaxConfig(
            intensity = 0.35f,
            maxTranslationY = 35f,
            scaleRange = 1.0f..1.02f,
            layerSpeed = 0.25f
        )

        /**
         * Ultra-subtle, non-distracting depth for Active Workout sessions.
         */
        val WorkoutSubtle = ParallaxConfig(
            intensity = 0.25f,
            maxTranslationY = 25f,
            scaleRange = 1.0f..1.01f,
            layerSpeed = 0.2f
        )

        /**
         * Smooth card depth for Charts and Progress records.
         */
        val ProgressSubtle = ParallaxConfig(
            intensity = 0.40f,
            maxTranslationY = 40f,
            scaleRange = 1.0f..1.03f,
            layerSpeed = 0.3f
        )

        /**
         * Generic subtle section depth.
         */
        val SubtleSection = ProgressSubtle

        /**
         * Minimal depth for Settings and Administrative screens.
         */
        val Minimal = ParallaxConfig(
            intensity = 0.15f,
            maxTranslationY = 15f,
            scaleRange = 1.0f..1.01f,
            layerSpeed = 0.1f
        )
    }
}
