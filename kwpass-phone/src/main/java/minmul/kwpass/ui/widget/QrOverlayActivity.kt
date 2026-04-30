package minmul.kwpass.ui.widget

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import minmul.kwpass.R
import minmul.kwpass.ui.main.MainViewModel
import minmul.kwpass.ui.theme.KWPassTheme
import kotlin.math.hypot
import kotlin.math.max

private const val OverlayEnterDurationMillis = 300
private const val OverlayExitDurationMillis = 180
private const val OverlayDismissScale = 0.86f
private const val OverlayDismissAlpha = 0.72f
private const val OverlayDismissScrimAlpha = 0.45f
private val OverlayEnterEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val OverlayExitEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
private val OverlayStateEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

private enum class QrOverlayContentState {
    Loading,
    Qr,
    Error,
    Initial,
    Empty
}

@AndroidEntryPoint
class QrOverlayActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)

        viewModel.refreshQR(onWidget = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KWPassTheme {
                val context = LocalContext.current
                val view = LocalView.current
                LaunchedEffect(key1 = true) {
                    viewModel.toastEvent.collect { uiText ->
                        val message = uiText.asString(context)
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

                val uiState by viewModel.mainUiState.collectAsStateWithLifecycle()
                val hasValidAccount by rememberUpdatedState(uiState.accountInfo.hasValidInfo)

                val lifecycleOwner = LocalLifecycleOwner.current

                val density = LocalDensity.current
                val scope = rememberCoroutineScope() // 애니메이션 실행용 스코프

                val offsetX = remember { Animatable(0f, Float.VectorConverter) }
                val offsetY = remember { Animatable(0f, Float.VectorConverter) }
                var overlayVisible by remember { mutableStateOf(false) }
                var closeRequested by remember { mutableStateOf(false) }
                var backProgress by remember { mutableFloatStateOf(0f) }
                var dragDismissHapticPlayed by remember { mutableStateOf(false) }

                val exitDistanceThreshold = with(density) { 80.dp.toPx() }
                val feedbackConstant = if (Build.VERSION.SDK_INT >= 34) {
                    HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
                } else if (Build.VERSION.SDK_INT >= 30) {
                    HapticFeedbackConstants.GESTURE_START
                } else {
                    HapticFeedbackConstants.VIRTUAL_KEY
                }

                LaunchedEffect(Unit) {
                    overlayVisible = true
                }

                fun requestClose() {
                    if (closeRequested) return

                    closeRequested = true
                    overlayVisible = false
                    scope.launch {
                        delay(OverlayExitDurationMillis.toLong())
                        finish()
                    }
                }

                fun resetDragOffset() {
                    scope.launch {
                        launch {
                            offsetX.animateTo(0f)
                        }
                        launch {
                            offsetY.animateTo(0f)
                        }
                    }
                }

                PredictiveBackHandler(enabled = !closeRequested) { progress ->
                    try {
                        progress.collect { backEvent ->
                            backProgress = backEvent.progress
                        }
                        requestClose()
                    } catch (e: CancellationException) {
                        backProgress = 0f
                        throw e
                    }
                }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Event.ON_RESUME) {
                            if (hasValidAccount) {
                                viewModel.refreshQR(onWidget = true)
                            }
                        }
                    }

                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                LaunchedEffect(uiState.process.qrBitmap) {
                    if (uiState.process.qrBitmap != null) {
                        while (isActive) {
                            delay(50000L)
                            viewModel.refreshQR(onWidget = true)
                        }
                    }
                }

                val overlayTransition = updateTransition(
                    targetState = overlayVisible,
                    label = "qr_overlay_visibility"
                )
                val scrimAlpha by overlayTransition.animateFloat(
                    transitionSpec = {
                        tween(
                            durationMillis = if (targetState) {
                                OverlayEnterDurationMillis
                            } else {
                                OverlayExitDurationMillis
                            },
                            easing = if (targetState) OverlayEnterEasing else OverlayExitEasing
                        )
                    },
                    label = "qr_overlay_scrim_alpha"
                ) { visible ->
                    if (visible) 0.6f else 0f
                }
                val containerAlpha by overlayTransition.animateFloat(
                    transitionSpec = {
                        tween(
                            durationMillis = if (targetState) {
                                OverlayEnterDurationMillis
                            } else {
                                OverlayExitDurationMillis / 2
                            },
                            easing = if (targetState) OverlayEnterEasing else OverlayExitEasing
                        )
                    },
                    label = "qr_overlay_container_alpha"
                ) { visible ->
                    if (visible) 1f else 0f
                }
                val containerScale by overlayTransition.animateFloat(
                    transitionSpec = {
                        tween(
                            durationMillis = if (targetState) {
                                OverlayEnterDurationMillis
                            } else {
                                OverlayExitDurationMillis
                            },
                            easing = OverlayStateEasing
                        )
                    },
                    label = "qr_overlay_container_scale"
                ) { visible ->
                    if (visible) 1f else 0.72f
                }
                val containerCornerRadius by overlayTransition.animateDp(
                    transitionSpec = {
                        tween(
                            durationMillis = if (targetState) {
                                OverlayEnterDurationMillis
                            } else {
                                OverlayExitDurationMillis
                            },
                            easing = OverlayStateEasing
                        )
                    },
                    label = "qr_overlay_container_corner"
                ) { visible ->
                    if (visible) 16.dp else 28.dp
                }

                val dragProgress = (
                        hypot(offsetX.value, offsetY.value) / exitDistanceThreshold
                        ).coerceIn(0f, 1f)
                val interactionProgress = max(dragProgress, backProgress)
                val interactiveScale = 1f - ((1f - OverlayDismissScale) * interactionProgress)
                val interactiveAlpha = 1f - ((1f - OverlayDismissAlpha) * interactionProgress)
                val interactiveScrimAlpha = 1f -
                        ((1f - OverlayDismissScrimAlpha) * interactionProgress)

                val qrBitmap = uiState.process.qrBitmap
                val contentState = when {
                    !uiState.accountDataLoaded || uiState.process.isFetching -> {
                        QrOverlayContentState.Loading
                    }

                    qrBitmap != null -> QrOverlayContentState.Qr
                    uiState.process.fetchFailed -> QrOverlayContentState.Error
                    uiState.process.initialStatus -> QrOverlayContentState.Initial
                    else -> QrOverlayContentState.Empty
                }

                // 전체 화면 반투명 박스
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = scrimAlpha * interactiveScrimAlpha))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragDismissHapticPlayed = false
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val nextOffsetX = offsetX.value + dragAmount.x
                                    val nextOffsetY = offsetY.value + dragAmount.y
                                    val nextDistance = hypot(nextOffsetX, nextOffsetY)

                                    if (
                                        nextDistance > exitDistanceThreshold &&
                                        !dragDismissHapticPlayed
                                    ) {
                                        dragDismissHapticPlayed = true
                                        view.performHapticFeedback(feedbackConstant)
                                    }

                                    scope.launch {
                                        offsetX.snapTo(nextOffsetX)
                                        offsetY.snapTo(nextOffsetY)
                                    }
                                },
                                onDragEnd = {
                                    val distance = hypot(offsetX.value, offsetY.value)

                                    if (distance > exitDistanceThreshold) {
                                        requestClose()
                                    } else {
                                        dragDismissHapticPlayed = false
                                        resetDragOffset()
                                    }

                                },
                                onDragCancel = {
                                    dragDismissHapticPlayed = false
                                    resetDragOffset()
                                }
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (viewModel.backAction()) {
                                    requestClose()
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = containerAlpha * interactiveAlpha
                                scaleX = containerScale * interactiveScale
                                scaleY = containerScale * interactiveScale
                            },
                        targetState = contentState,
                        transitionSpec = {
                            val enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = OverlayExitDurationMillis,
                                    easing = OverlayEnterEasing
                                )
                            ) + scaleIn(
                                initialScale = 0.96f,
                                animationSpec = tween(
                                    durationMillis = OverlayExitDurationMillis,
                                    easing = OverlayEnterEasing
                                )
                            )

                            val exit = fadeOut(
                                animationSpec = tween(
                                    durationMillis = OverlayExitDurationMillis,
                                    easing = OverlayExitEasing
                                )
                            ) + scaleOut(
                                targetScale = 0.96f,
                                animationSpec = tween(
                                    durationMillis = OverlayExitDurationMillis,
                                    easing = OverlayExitEasing
                                )
                            )

                            enter.togetherWith(exit)
                        },
                        label = "qr_overlay_content"
                    ) { state ->
                        Box(
                            modifier = Modifier
                                .sizeIn(minWidth = 220.dp, minHeight = 220.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (state) {
                                QrOverlayContentState.Loading -> {
                                    CircularProgressIndicator(color = Color.White)
                                }

                                QrOverlayContentState.Qr -> {
                                    if (qrBitmap != null) {
                                        KeepScreenMaxBrightness()
                                        Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "QR Code",
                                            modifier = Modifier
                                                .size(uiState.process.qrSize.dp)
                                                .clip(RoundedCornerShape(containerCornerRadius)),
                                            filterQuality = FilterQuality.None
                                        )
                                    } else {
                                        CircularProgressIndicator(
                                            color = Color.White
                                        )
                                    }
                                }

                                QrOverlayContentState.Error -> {
                                    Text(
                                        text = stringResource(R.string.error_common),
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                QrOverlayContentState.Initial -> {
                                    Text(
                                        text = stringResource(R.string.initial_account_setup_desc),
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                QrOverlayContentState.Empty -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeepScreenMaxBrightness() {
    val context = LocalContext.current
    val window = (context as? Activity)?.window ?: return

    val isInspection = LocalInspectionMode.current
    if (isInspection) return

    DisposableEffect(Unit) {
        val originalAttributes = window.attributes
        val originalBrightness = originalAttributes.screenBrightness

        val newAttributes = window.attributes
        newAttributes.screenBrightness = 1f // 최대 밝기
        window.attributes = newAttributes

        onDispose {
            newAttributes.screenBrightness = originalBrightness
            window.attributes = newAttributes
        }
    }
}
