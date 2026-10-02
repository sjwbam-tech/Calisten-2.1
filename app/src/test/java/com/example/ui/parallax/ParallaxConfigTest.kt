package com.example.ui.parallax

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParallaxConfigTest {

    @Test
    fun testDefaultParallaxCalculations_normalMotion() {
        val config = ParallaxConfig(
            intensity = 1.0f,
            maxTranslationY = 100f,
            scaleRange = 1.0f..1.10f,
            alphaRange = 1.0f..0.20f,
            layerSpeed = 0.5f,
            reduceMotion = false
        )

        // At scroll fraction 0.0
        val trans0 = config.computeTranslationY(0f, isReducedMotion = false)
        val scale0 = config.computeScale(0f, isReducedMotion = false)
        val alpha0 = config.computeAlpha(0f, isReducedMotion = false)

        assertEquals(0f, trans0, 0.001f)
        assertEquals(1.0f, scale0, 0.001f)
        assertEquals(1.0f, alpha0, 0.001f)

        // At scroll fraction 0.5
        val transHalf = config.computeTranslationY(0.5f, isReducedMotion = false)
        val scaleHalf = config.computeScale(0.5f, isReducedMotion = false)

        assertEquals(-25f, transHalf, 0.001f)
        assertEquals(1.05f, scaleHalf, 0.001f)

        // At scroll fraction 1.0
        val trans1 = config.computeTranslationY(1.0f, isReducedMotion = false)
        val scale1 = config.computeScale(1.0f, isReducedMotion = false)
        val alpha1 = config.computeAlpha(1.0f, isReducedMotion = false)

        assertEquals(-50f, trans1, 0.001f)
        assertEquals(1.10f, scale1, 0.001f)
        assertEquals(0.20f, alpha1, 0.001f)
    }

    @Test
    fun testReducedMotion_disablesAllTranslationsAndScales() {
        val config = ParallaxConfig.HeroCinematic

        // When isReducedMotion param is true
        val transReduced = config.computeTranslationY(0.8f, isReducedMotion = true)
        val scaleReduced = config.computeScale(0.8f, isReducedMotion = true)
        val alphaReduced = config.computeAlpha(0.8f, isReducedMotion = true)

        assertEquals(0f, transReduced, 0.001f)
        assertEquals(1.0f, scaleReduced, 0.001f)
        assertEquals(1.0f, alphaReduced, 0.001f)

        // When config itself has reduceMotion = true
        val configReduced = config.copy(reduceMotion = true)
        val transFromConfig = configReduced.computeTranslationY(0.8f, isReducedMotion = false)
        val scaleFromConfig = configReduced.computeScale(0.8f, isReducedMotion = false)
        val alphaFromConfig = configReduced.computeAlpha(0.8f, isReducedMotion = false)

        assertEquals(0f, transFromConfig, 0.001f)
        assertEquals(1.0f, scaleFromConfig, 0.001f)
        assertEquals(1.0f, alphaFromConfig, 0.001f)
    }

    @Test
    fun testPresets_haveConsistentDepthHierarchy() {
        // Hero has strongest intensity and translation
        assertTrue(ParallaxConfig.HeroCinematic.maxTranslationY > ParallaxConfig.SkillsCinematic.maxTranslationY)
        assertTrue(ParallaxConfig.SkillsCinematic.maxTranslationY > ParallaxConfig.LibraryHeader.maxTranslationY)
        assertTrue(ParallaxConfig.LibraryHeader.maxTranslationY > ParallaxConfig.BuilderSubtle.maxTranslationY)
        assertTrue(ParallaxConfig.BuilderSubtle.maxTranslationY > ParallaxConfig.WorkoutSubtle.maxTranslationY)
        assertTrue(ParallaxConfig.WorkoutSubtle.maxTranslationY > ParallaxConfig.Minimal.maxTranslationY)

        // Scale bounds are positive and greater than 1.0f
        assertTrue(ParallaxConfig.HeroCinematic.scaleRange.endInclusive > 1.0f)
        assertTrue(ParallaxConfig.Minimal.scaleRange.endInclusive >= 1.0f)
    }
}
