package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.CameraColors
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.ScanHitColor
import kotlin.math.PI
import kotlin.math.sin

enum class ViewfinderShape { Square, Wide }

/**
 * Frame rectangle (px) the overlay draws inside a canvas of the given size. The scan screen uses the same
 * function to crop the analysis region when "scan area only" is on, so the overlay and the decoder agree.
 */
fun viewfinderRect(canvasWidth: Float, canvasHeight: Float, density: Float, shape: ViewfinderShape): Rect {
    val maxWidth = canvasWidth - 48f * density
    val w = minOf(if (shape == ViewfinderShape.Square) 280f * density else 312f * density, maxWidth)
    val h = if (shape == ViewfinderShape.Square) w else w * (168f / 312f)
    val topFraction = if (shape == ViewfinderShape.Square) 0.22f else 0.28f
    val left = (canvasWidth - w) / 2f
    val top = canvasHeight * topFraction
    return Rect(left, top, left + w, top + h)
}

/**
 * Camera viewfinder: dimmed surroundings, four rounded corner brackets (white, turning teal over 150 ms when
 * [detected]) and a scan line sweeping top to bottom every 1.8 s while [scanning]. Purely decorative: it does
 * not intercept touches and has no semantics.
 */
@Composable
fun ViewfinderOverlay(
    modifier: Modifier = Modifier,
    shape: ViewfinderShape = ViewfinderShape.Square,
    detected: Boolean = false,
    scanning: Boolean = true,
    dimOutside: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "scanLine")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1800, easing = LinearEasing), RepeatMode.Restart),
        label = "scanLineProgress",
    )
    val bracketColor by animateColorAsState(if (detected) ScanHitColor else Color.White, tween(150), label = "bracketColor")

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        val rect = viewfinderRect(size.width, size.height, density, shape)
        val radius = 22.dp.toPx()
        if (dimOutside) {
            drawRect(CameraColors.dim)
            drawRoundRect(
                color = Color.Black,
                topLeft = rect.topLeft,
                size = Size(rect.width, rect.height),
                cornerRadius = CornerRadius(radius, radius),
                blendMode = BlendMode.Clear,
            )
        }
        val len = 44.dp.toPx()
        val l = rect.left
        val t = rect.top
        val r = rect.right
        val b = rect.bottom
        val corners = Path().apply {
            moveTo(l, t + len); lineTo(l, t + radius)
            arcTo(Rect(l, t, l + 2 * radius, t + 2 * radius), 180f, 90f, false); lineTo(l + len, t)

            moveTo(r - len, t); lineTo(r - radius, t)
            arcTo(Rect(r - 2 * radius, t, r, t + 2 * radius), 270f, 90f, false); lineTo(r, t + len)

            moveTo(r, b - len); lineTo(r, b - radius)
            arcTo(Rect(r - 2 * radius, b - 2 * radius, r, b), 0f, 90f, false); lineTo(r - len, b)

            moveTo(l + len, b); lineTo(l + radius, b)
            arcTo(Rect(l, b - 2 * radius, l + 2 * radius, b), 90f, 90f, false); lineTo(l, b - len)
        }
        drawPath(corners, bracketColor, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))

        if (scanning && !detected) {
            val y = t + rect.height * (0.06f + 0.88f * progress)
            val alpha = sin(PI.toFloat() * progress).coerceIn(0f, 1f)
            val x0 = l + 10.dp.toPx()
            val x1 = r - 10.dp.toPx()
            val brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, CameraColors.scanLine.copy(alpha = alpha), Color.Transparent),
                startX = x0,
                endX = x1,
            )
            drawLine(brush, Offset(x0, y), Offset(x1, y), strokeWidth = 14.dp.toPx(), alpha = 0.25f * alpha)
            drawLine(brush, Offset(x0, y), Offset(x1, y), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

@Preview(name = "Viewfinder", widthDp = 360, heightDp = 640)
@Composable
private fun ViewfinderPreview() {
    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().background(Color(0xFF1B1F35))) {
        ViewfinderOverlay()
    }
}

@Preview(name = "Viewfinder detected wide", widthDp = 360, heightDp = 640)
@Composable
private fun ViewfinderDetectedPreview() {
    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().background(Color(0xFF1B1F35))) {
        ViewfinderOverlay(shape = ViewfinderShape.Wide, detected = true)
    }
}
