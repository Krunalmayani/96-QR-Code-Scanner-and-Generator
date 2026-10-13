package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme

/** The three onboarding slides (artboards s02 to s04). */
internal enum class OnboardingSlide(@StringRes val title: Int, @StringRes val body: Int) {
    Scan(R.string.onb_slide1_title, R.string.onb_slide1_body),
    Create(R.string.onb_slide2_title, R.string.onb_slide2_body),
    Privacy(R.string.onb_slide3_title, R.string.onb_slide3_body),
}

private val HeroShape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)

/** One pager page: gradient hero with the slide illustration, then headline and body text. */
@Composable
internal fun SlidePage(slide: OnboardingSlide, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        when (slide) {
            OnboardingSlide.Scan -> SlideHero(Modifier.weight(1f), stageWidth = 240.dp, stageHeight = 300.dp) { ScanStage() }
            OnboardingSlide.Create -> SlideHero(Modifier.weight(1f), stageWidth = 320.dp, stageHeight = 294.dp) { CreateStage() }
            OnboardingSlide.Privacy -> SlideHero(Modifier.weight(1f), stageWidth = 320.dp, stageHeight = 308.dp) { PrivacyStage() }
        }
        SlideText(slide)
    }
}

@Composable
private fun SlideText(slide: OnboardingSlide) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 32.dp)) {
        Text(
            text = stringResource(slide.title),
            style = AppTheme.type.display.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold),
            color = c.ink,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(slide.body),
            style = AppTheme.type.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = c.inkSecondary,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

/**
 * Gradient hero with rounded bottom corners. The illustration is laid out in a fixed
 * [stageWidth] x [stageHeight] stage that shrinks (never grows) to fit short or narrow screens.
 */
@Composable
private fun SlideHero(
    modifier: Modifier,
    stageWidth: Dp,
    stageHeight: Dp,
    stage: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxWidth().clip(HeroShape).background(OnboardingColors.heroBrush)) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-70).dp)
                .size(240.dp)
                .background(OnboardingColors.glow, CircleShape),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 80.dp)
                .size(240.dp)
                .background(OnboardingColors.glow, CircleShape),
        )
        BoxWithConstraints(Modifier.fillMaxSize().statusBarsPadding().padding(vertical = 16.dp)) {
            val scale = minOf(1f, maxWidth / stageWidth, maxHeight / stageHeight)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .requiredSize(stageWidth, stageHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                content = stage,
            )
        }
    }
}

// Slide 1: a code inside the viewfinder brackets and a "Code found" chip.
@Composable
private fun BoxScope.ScanStage() {
    Box(Modifier.align(Alignment.TopCenter).size(240.dp)) {
        DetectedBrackets(Modifier.fillMaxSize())
        Box(
            Modifier
                .align(Alignment.Center)
                .size(188.dp)
                .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = OnboardingColors.cardShadow, spotColor = OnboardingColors.cardShadow)
                .background(Color.White, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            QrIllustration(dimension = 160.dp, styled = false)
        }
    }
    HeroChip(
        text = stringResource(R.string.onb_slide1_chip),
        icon = AppIcons.Check,
        textColor = OnboardingColors.chipSuccessText,
        iconColor = OnboardingColors.chipSuccessIcon,
        modifier = Modifier.align(Alignment.BottomCenter),
    )
}

// Slide 2: a styled code surrounded by the code types that can be created.
@Composable
private fun BoxScope.CreateStage() {
    Box(
        Modifier
            .align(Alignment.TopStart)
            .offset(x = 70.dp, y = 34.dp)
            .size(180.dp)
            .shadow(20.dp, RoundedCornerShape(28.dp), ambientColor = OnboardingColors.cardShadow, spotColor = OnboardingColors.cardShadow)
            .background(Color.White, RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center,
    ) {
        QrIllustration(dimension = 148.dp, styled = true)
    }
    TypeTile(AppIcons.Link, x = 24.dp, y = 0.dp)
    TypeTile(AppIcons.Wifi, x = 240.dp, y = 0.dp)
    TypeTile(AppIcons.Person, x = 4.dp, y = 122.dp)
    TypeTile(AppIcons.Calendar, x = 260.dp, y = 122.dp)
    TypeTile(AppIcons.Phone, x = 40.dp, y = 238.dp)
    TypeTile(AppIcons.Text, x = 224.dp, y = 238.dp)
}

// Slide 3: shield with a lock and two chips about local storage.
@Composable
private fun BoxScope.PrivacyStage() {
    Box(
        Modifier
            .align(Alignment.TopStart)
            .offset(x = 72.dp)
            .size(176.dp)
            .shadow(20.dp, CircleShape, ambientColor = OnboardingColors.cardShadow, spotColor = OnboardingColors.cardShadow)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(AppIcons.Shield, contentDescription = null, tint = OnboardingColors.tileIcon, modifier = Modifier.size(104.dp))
        Icon(
            AppIcons.Lock,
            contentDescription = null,
            tint = OnboardingColors.tileIcon,
            modifier = Modifier.size(36.dp).offset(y = (-4).dp),
        )
    }
    HeroChip(
        text = stringResource(R.string.onb_slide3_chip_saved),
        icon = AppIcons.History,
        textColor = OnboardingColors.chipPrimaryText,
        iconColor = OnboardingColors.chipPrimaryText,
        modifier = Modifier.align(Alignment.TopStart).offset(x = 16.dp, y = 212.dp),
    )
    HeroChip(
        text = stringResource(R.string.onb_slide3_chip_delete),
        icon = AppIcons.Delete,
        textColor = OnboardingColors.chipPrimaryText,
        iconColor = OnboardingColors.chipPrimaryText,
        modifier = Modifier.align(Alignment.TopStart).offset(x = 130.dp, y = 268.dp),
    )
}

@Composable
private fun BoxScope.TypeTile(icon: ImageVector, x: Dp, y: Dp) {
    Box(
        Modifier
            .align(Alignment.TopStart)
            .offset(x = x, y = y)
            .size(56.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp))
            .background(Color.White, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = OnboardingColors.tileIcon, modifier = Modifier.size(24.dp))
    }
}

/** White pill on the hero: icon plus short label. */
@Composable
private fun HeroChip(
    text: String,
    icon: ImageVector,
    textColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .background(OnboardingColors.chipSurface, RoundedCornerShape(20.dp))
            .padding(start = 12.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Text(text, style = AppTheme.type.bodyStrong, color = textColor, maxLines = 1)
    }
}

/** Four rounded corner brackets in the "code detected" teal. */
@Composable
private fun DetectedBrackets(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val radius = 22.dp.toPx()
        val len = 44.dp.toPx()
        val stroke = 2.dp.toPx()
        val l = stroke
        val t = stroke
        val r = size.width - stroke
        val b = size.height - stroke
        val corners = Path().apply {
            moveTo(l, t + len)
            lineTo(l, t + radius)
            arcTo(Rect(l, t, l + 2 * radius, t + 2 * radius), 180f, 90f, false)
            lineTo(l + len, t)

            moveTo(r - len, t)
            lineTo(r - radius, t)
            arcTo(Rect(r - 2 * radius, t, r, t + 2 * radius), 270f, 90f, false)
            lineTo(r, t + len)

            moveTo(r, b - len)
            lineTo(r, b - radius)
            arcTo(Rect(r - 2 * radius, b - 2 * radius, r, b), 0f, 90f, false)
            lineTo(r - len, b)

            moveTo(l + len, b)
            lineTo(l + radius, b)
            arcTo(Rect(l, b - 2 * radius, l + 2 * radius, b), 90f, 90f, false)
            lineTo(l, b - len)
        }
        drawPath(corners, OnboardingColors.detectedBracket, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
    }
}
