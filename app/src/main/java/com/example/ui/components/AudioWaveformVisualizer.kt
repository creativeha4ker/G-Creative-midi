package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AudioWaveformVisualizer(
    amplitudes: FloatArray,
    isDemoActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MahoganyPanel)
            .border(1.dp, MahoganyBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isDemoActive) StudioCyan else StudioAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDemoActive) "PCM 44.1kHz / 24-bit STREAMING..." else "DSP REAL-TIME HARMONIC SPECTRUM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold80
                )
            }

            Text(
                text = "EQ: 2-5kHz Clarity (+3dB)",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = StudioCyan
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0D0A08))
                .border(1.dp, Color(0xFF261E18), RoundedCornerShape(8.dp))
        ) {
            val width = size.width
            val height = size.height
            val barCount = 32
            val barGap = 3f
            val barWidth = (width - (barCount - 1) * barGap) / barCount

            for (i in 0 until barCount) {
                val amp = if (i < amplitudes.size) amplitudes[i] else 0.1f
                val animatedAmp = (amp + 0.05f * kotlin.math.sin(pulsePhase + i * 0.3f).toFloat()).coerceIn(0.05f, 1.0f)
                val barHeight = animatedAmp * height

                val x = i * (barWidth + barGap)
                val y = height - barHeight

                val gradient = Brush.verticalGradient(
                    colors = listOf(StudioCyan, Gold60, StudioAmber),
                    startY = y,
                    endY = height
                )

                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }

            // Draw center baseline
            drawLine(
                color = Color(0x33D4AF37),
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = 1f
            )
        }
    }
}
