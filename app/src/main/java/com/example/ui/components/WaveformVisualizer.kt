package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PalashAccentLime
import com.example.ui.theme.PalashSecondaryLight

@Composable
fun WaveformVisualizer(
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 16
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val baseHeights = listOf(
            12.dp, 24.dp, 36.dp, 18.dp, 44.dp, 28.dp, 52.dp, 32.dp,
            48.dp, 20.dp, 40.dp, 26.dp, 34.dp, 16.dp, 28.dp, 14.dp
        )

        for (i in 0 until barCount) {
            val animDuration = 400 + (i * 70) % 500
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            val currentHeight = if (isRecording) {
                val maxH = baseHeights[i % baseHeights.size]
                maxH * (0.3f + 0.7f * scale)
            } else {
                8.dp
            }

            val brush = Brush.verticalGradient(
                colors = if (isRecording) {
                    listOf(PalashSecondaryLight, PalashAccentLime)
                } else {
                    listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.2f))
                }
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(currentHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(brush)
            )
        }
    }
}
