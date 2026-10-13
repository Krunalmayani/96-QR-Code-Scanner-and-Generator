package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PageDots
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import kotlinx.coroutines.launch

/** Swipe and Next animation length from the motion notes (PRD 14). */
private const val PAGE_ANIMATION_MS = 300

/** 300 ms ease-out, shared by swipe settling and the Next / Back buttons. */
private val PageSpec = tween<Float>(durationMillis = PAGE_ANIMATION_MS, easing = EaseOut)

/**
 * Stateful entry of the three slides. [onFinished] runs for both "Skip" and the last slide's "Get started":
 * the caller moves on to the camera rationale step.
 */
@Composable
internal fun OnboardingRoute(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { OnboardingSlide.entries.size })
    val scope = rememberCoroutineScope()
    val lastPage = OnboardingSlide.entries.size - 1

    fun goTo(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page, animationSpec = PageSpec) }
    }

    BackHandler(enabled = pagerState.currentPage > 0) { goTo(pagerState.currentPage - 1) }

    OnboardingScreen(
        pagerState = pagerState,
        onSkip = onFinished,
        onBack = { goTo(pagerState.currentPage - 1) },
        onNext = { if (pagerState.currentPage >= lastPage) onFinished() else goTo(pagerState.currentPage + 1) },
    )
}

/** Stateless slides screen (s02 to s04): pager, page dots, Skip, Back and Next / Get started. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun OnboardingScreen(
    pagerState: PagerState,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = OnboardingSlide.entries.size
    val page = pagerState.currentPage
    val fling =
        PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = PageSpec,
        )
    Box(modifier.fillMaxSize().background(AppTheme.colors.background)) {
        Column(Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                flingBehavior = fling,
            ) { index ->
                SlidePage(OnboardingSlide.entries[index])
            }
            val pageDescription = stringResource(R.string.onb_page_indicator, page + 1, count)
            PageDots(
                count = count,
                selected = page,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 24.dp)
                        .semantics {
                            contentDescription = pageDescription
                            liveRegion = LiveRegionMode.Polite
                        },
            )
            NavigationRow(page = page, lastPage = count - 1, onBack = onBack, onNext = onNext)
        }
        if (page < count - 1) {
            SkipButton(
                onClick = onSkip,
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 8.dp, end = 12.dp),
            )
        }
    }
}

@Composable
private fun NavigationRow(page: Int, lastPage: Int, onBack: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (page > 0) {
            AppTextButton(
                text = stringResource(R.string.common_back),
                onClick = onBack,
                modifier = Modifier.heightIn(min = 56.dp),
            )
        }
        val nextLabel = if (page == lastPage) R.string.onb_get_started else R.string.common_next
        PrimaryButton(
            text = stringResource(nextLabel),
            onClick = onNext,
            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
        )
    }
}

/** "Skip" in white on the hero (the design-system text button is primary colored, unreadable on the gradient). */
@Composable
private fun SkipButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.common_skip),
            style = AppTheme.type.button,
            color = OnboardingColors.onHero,
        )
    }
}

@Composable
private fun PreviewSlide(page: Int, dark: Boolean) {
    AppTheme(dark = dark) {
        val state = rememberPagerState(initialPage = page, pageCount = { OnboardingSlide.entries.size })
        OnboardingScreen(pagerState = state, onSkip = {}, onBack = {}, onNext = {})
    }
}

@Preview(name = "S02_Slide1Scan_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S02_Slide1Scan_Light() = PreviewSlide(page = 0, dark = false)

@Preview(name = "S02_Slide1Scan_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S02_Slide1Scan_Dark() = PreviewSlide(page = 0, dark = true)

@Preview(name = "S03_Slide2Create_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S03_Slide2Create_Light() = PreviewSlide(page = 1, dark = false)

@Preview(name = "S03_Slide2Create_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S03_Slide2Create_Dark() = PreviewSlide(page = 1, dark = true)

@Preview(name = "S04_Slide3Privacy_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S04_Slide3Privacy_Light() = PreviewSlide(page = 2, dark = false)

@Preview(name = "S04_Slide3Privacy_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S04_Slide3Privacy_Dark() = PreviewSlide(page = 2, dark = true)
