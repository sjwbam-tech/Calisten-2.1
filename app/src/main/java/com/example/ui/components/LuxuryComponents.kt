package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.IceBlueGlow
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.Obsidian700
import com.example.ui.theme.Obsidian800
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950

@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassCardBorderSubtle,
    backgroundColor: Color = GlassCardBackground,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val cardModifier = modifier
        .clip(shape)
        .background(backgroundColor)
        .border(1.dp, borderColor, shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Box(modifier = cardModifier) {
        content()
    }
}

@Composable
fun PersianTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Surface(
        color = Obsidian950.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("topbar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = FrostWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = FrostWhite,
                        fontWeight = FontWeight.Bold
                    )
                    if (!subtitle.isNullOrEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = IceBlueLight
                        )
                    }
                }
            }

            if (actions != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    actions()
                }
            }
        }
    }
}

@Composable
fun IceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTag: String = "ice_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = IceBluePrimary,
            contentColor = Obsidian950,
            disabledContainerColor = Obsidian700,
            disabledContentColor = MutedSlate
        ),
        modifier = modifier
            .height(50.dp)
            .testTag(testTag)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Obsidian950,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun IceOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "ice_outline_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(IceBluePrimary, IceBlueGlow))
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = IceBlueLight
        ),
        modifier = modifier
            .height(50.dp)
            .testTag(testTag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatBadge(
    title: String,
    value: String,
    unit: String = "",
    highlightColor: Color = IceBluePrimary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Obsidian800)
            .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MutedSlate
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = highlightColor
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Pure Canvas Chart based strictly on real recorded data.
 * Adheres strictly to the specification requirement:
 * If points < 2, display exact Persian string:
 * «برای نمایش این نمودار هنوز داده کافی ثبت نشده است.»
 */
@Composable
fun RealDataLineChart(
    dataPoints: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = IceBluePrimary,
    unitLabel: String = ""
) {
    if (dataPoints.size < 2) {
        // Insufficient real data state
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Obsidian900)
                .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(14.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MutedSlate,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "برای نمایش این نمودار هنوز داده کافی ثبت نشده است.",
                style = MaterialTheme.typography.bodyMedium,
                color = FrostWhite,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "با ثبت حداقل ۲ رکورد یا جلسه واقعی، نمودار تحلیلی به طور خودکار ترسیم می‌شود.",
                style = MaterialTheme.typography.labelSmall,
                color = MutedSlate,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Render genuine canvas chart
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Obsidian900)
                .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            val minVal = dataPoints.minOrNull() ?: 0f
            val maxVal = dataPoints.maxOrNull() ?: 100f
            val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "آخرین رکورد: ${dataPoints.last()} $unitLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = IceBlueLight,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "بیشینه: $maxVal $unitLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val width = size.width
                val height = size.height
                val stepX = width / (dataPoints.size - 1)

                val points = dataPoints.mapIndexed { index, value ->
                    val x = index * stepX
                    val y = height - ((value - minVal) / range * (height - 30.dp.toPx())) - 15.dp.toPx()
                    Offset(x, y)
                }

                // Fill gradient under path
                val fillPath = Path().apply {
                    moveTo(points.first().x, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Line path
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw dots
                points.forEach { point ->
                    drawCircle(
                        color = Obsidian950,
                        radius = 5.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 3.5.dp.toPx(),
                        center = point
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = labels.firstOrNull() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontSize = 10.sp
                )
                if (labels.size > 2) {
                    Text(
                        text = labels[labels.size / 2],
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSlate,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = labels.lastOrNull() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontSize = 10.sp
                )
            }
        }
    }
}
