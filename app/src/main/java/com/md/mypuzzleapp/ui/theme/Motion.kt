package com.md.mypuzzleapp.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween

// Subtle motion specs to be used for interactive feedback
object Motion {
    val emphasize: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val decelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)

    // 120–200ms for snappy interactions
    fun fast() = tween<Int>(durationMillis = 120, easing = standard)
    fun medium() = tween<Int>(durationMillis = 180, easing = standard)
    fun slow() = tween<Int>(durationMillis = 240, easing = decelerate)
}
