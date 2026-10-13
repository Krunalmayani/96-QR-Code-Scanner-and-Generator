package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ViewfinderShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.viewfinderRect
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.PxRect
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ScanMode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ZoomMath
import java.util.concurrent.Executors

/** What the bound camera can do; read after binding. */
data class CameraCapabilities(val hasFlash: Boolean = false, val maxZoomRatio: Float = 1f)

/**
 * CameraX preview + ML Kit analysis. Binds to the current lifecycle owner, unbinds (torch off) when it leaves the
 * composition, restricts the decoder to [mode]'s symbologies and, when [scanAreaOnly] is on, ignores codes outside the
 * viewfinder frame (same [viewfinderRect] the overlay draws). While [frozen] the last frame stays on screen.
 */
@Composable
fun CameraPreview(
    mode: ScanMode,
    frontCamera: Boolean,
    torchOn: Boolean,
    zoom: Float,
    analyzing: Boolean,
    scanAreaOnly: Boolean,
    frozen: Boolean,
    onZoomChange: (Float) -> Unit,
    onCodes: (List<DetectedCode>) -> Unit,
    onCameraReady: (CameraCapabilities) -> Unit,
    onCameraError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current.density
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(analysisExecutor) { onDispose { analysisExecutor.shutdown() } }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var analysis by remember { mutableStateOf<ImageAnalysis?>(null) }
    var analyzer by remember { mutableStateOf<BarcodeAnalyzer?>(null) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var frozenFrame by remember { mutableStateOf<ImageBitmap?>(null) }

    val latestOnCodes by rememberUpdatedState(onCodes)
    val latestOnReady by rememberUpdatedState(onCameraReady)
    val latestOnError by rememberUpdatedState(onCameraError)
    val latestZoom by rememberUpdatedState(zoom)
    val latestOnZoom by rememberUpdatedState(onZoomChange)

    DisposableEffect(lifecycleOwner, frontCamera) {
        var provider: ProcessCameraProvider? = null
        var disposed = false
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                if (disposed) return@addListener
                try {
                    val cameraProvider = future.get()
                    provider = cameraProvider
                    val selector = if (frontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                    val useCaseAnalysis = buildAnalysis()
                    cameraProvider.unbindAll()
                    val bound = cameraProvider.bindToLifecycle(lifecycleOwner, selector, buildPreview(previewView), useCaseAnalysis)
                    analysis = useCaseAnalysis
                    camera = bound
                    latestOnReady(
                        CameraCapabilities(
                            hasFlash = bound.cameraInfo.hasFlashUnit(),
                            maxZoomRatio = bound.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f,
                        ),
                    )
                } catch (e: Exception) {
                    latestOnError()
                }
            },
            ContextCompat.getMainExecutor(context),
        )
        onDispose {
            disposed = true
            analysis = null
            camera = null
            provider?.unbindAll()
        }
    }

    DisposableEffect(analysis, mode) {
        val useCase = analysis
        var activeScanner: BarcodeScanner? = null
        if (useCase != null) {
            val scanner = BarcodeScanning.getClient(scannerOptionsFor(mode))
            val created = BarcodeAnalyzer(scanner) { codes -> latestOnCodes(codes) }
            activeScanner = scanner
            useCase.setAnalyzer(analysisExecutor, created)
            analyzer = created
        }
        onDispose {
            analyzer = null
            if (useCase != null) useCase.clearAnalyzer()
            activeScanner?.close()
        }
    }

    val areaSpec = remember(viewSize, scanAreaOnly, mode) {
        if (scanAreaOnly && viewSize.width > 0 && viewSize.height > 0) {
            val shape = if (mode.wideFrame) ViewfinderShape.Wide else ViewfinderShape.Square
            val frame = viewfinderRect(viewSize.width.toFloat(), viewSize.height.toFloat(), density, shape)
            ScanAreaSpec(PxRect(frame.left, frame.top, frame.right, frame.bottom), viewSize.width.toFloat(), viewSize.height.toFloat())
        } else {
            null
        }
    }
    val currentAnalyzer = analyzer
    SideEffect {
        currentAnalyzer?.enabled = analyzing
        currentAnalyzer?.area = areaSpec
    }

    LaunchedEffect(camera, torchOn) {
        val bound = camera ?: return@LaunchedEffect
        if (bound.cameraInfo.hasFlashUnit()) bound.cameraControl.enableTorch(torchOn)
    }
    LaunchedEffect(camera, zoom) {
        camera?.cameraControl?.setLinearZoom(zoom.coerceIn(0f, 1f))
    }
    LaunchedEffect(frozen) {
        frozenFrame = if (frozen) runCatching { previewView.bitmap?.asImageBitmap() }.getOrNull() else null
    }

    Box(modifier.fillMaxSize().onSizeChanged { viewSize = it }) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        frozenFrame?.let { frame ->
            Image(bitmap = frame, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        // Transparent layer on top that turns two-finger pinches into zoom.
        Box(
            Modifier.fillMaxSize().pointerInput(Unit) {
                detectTransformGestures { _, _, zoomChange, _ ->
                    if (zoomChange != 1f) latestOnZoom(ZoomMath.applyPinch(latestZoom, zoomChange))
                }
            },
        )
    }
}

private const val ANALYSIS_WIDTH = 1280
private const val ANALYSIS_HEIGHT = 960

private fun buildPreview(view: PreviewView): Preview {
    val selector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
        .build()
    return Preview.Builder().setResolutionSelector(selector).build().also { it.setSurfaceProvider(view.surfaceProvider) }
}

private fun buildAnalysis(): ImageAnalysis {
    val selector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
        .setResolutionStrategy(ResolutionStrategy(Size(ANALYSIS_WIDTH, ANALYSIS_HEIGHT), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
        .build()
    return ImageAnalysis.Builder()
        .setResolutionSelector(selector)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
}
