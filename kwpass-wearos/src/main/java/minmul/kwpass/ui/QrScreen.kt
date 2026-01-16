package minmul.kwpass.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import minmul.kwpass.R
import minmul.kwpass.ui.main.MainUiState
import minmul.kwpass.ui.main.MainUiStateProvider

@Composable
fun QrScreen(
    uiState: MainUiState,
    onRefresh: () -> Unit,
    stopTimer: () -> Unit,
    resumeTimer: () -> Unit
) {

    val qrAlpha by animateFloatAsState(
        targetValue = if (uiState.isRefreshing) 0.1f else 1.0f,
        animationSpec = tween(durationMillis = 300)
    )
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing)
        ), label = "spinAngle"
    )
    val interactionSource = remember { MutableInteractionSource() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> stopTimer()
                Lifecycle.Event.ON_RESUME -> resumeTimer()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (uiState.status !in listOf<ScreenStatus>(
                    ScreenStatus.START,
                    ScreenStatus.NOT_CONNECTED_TO_PHONE
                )
            ) {
                KeepScreenOn()
            }

            if (uiState.status != ScreenStatus.QR_READY) {
                Text(
                    text =
                        when (uiState.status) {
                            ScreenStatus.START -> stringResource(R.string.welcome)
                            ScreenStatus.NOT_CONNECTED_TO_PHONE -> stringResource(R.string.no_connected_phone)
                            ScreenStatus.FAILED_TO_GET_QR -> stringResource(R.string.failed_to_get_qr)
                            ScreenStatus.FETCHING_QR -> stringResource(R.string.fetching_qr)
                            ScreenStatus.SYNCING_ACCOUNT_DATA -> stringResource(R.string.loading_account)
                            ScreenStatus.FAILED_TO_GET_ACCOUNT_DATA_FROM_PHONE -> stringResource(
                                R.string.failed_to_get_account_from_phone
                            )

                            else -> ""
                        },
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    softWrap = true,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }
            if (uiState.savedQrBitmap != null) {
                if (!uiState.isRefreshing) {
                    KeepScreenMaxBrightness()
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .fillMaxSize(0.707f)
                        .alpha(qrAlpha)
                        .background(Color.White)
                        .zIndex(1f)
                        .clickable(
                            onClick = onRefresh,
                            enabled = !uiState.isRefreshing,
                            indication = null,
                            interactionSource = interactionSource
                        )
                ) {
                    Image(
                        bitmap = uiState.savedQrBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        filterQuality = FilterQuality.None

                    )
                }
            }


            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.isRefreshing || uiState.status in listOf<ScreenStatus>(
                        ScreenStatus.QR_READY,
                        ScreenStatus.NOT_CONNECTED_TO_PHONE,
                        ScreenStatus.FAILED_TO_GET_QR,
                        ScreenStatus.FETCHING_QR,
                        ScreenStatus.GENERATING_QR
                    )
                ) {
                    IconButton(
                        modifier = Modifier,
                        onClick = onRefresh,
                        enabled = !uiState.isRefreshing,
                        interactionSource = interactionSource
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(if (uiState.isRefreshing) angle else 0f),
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = if (uiState.isRefreshing) Color.LightGray else Color.White
                        )
                    }

                }
                if (uiState.status == ScreenStatus.QR_READY) {
                    Text(
                        text = stringResource(R.string.qr_seconds, uiState.refreshTimeLeft)
                    )
                }
            }

        }
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    name = "Large Round"
)
@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    name = "Square"
)
@Composable
fun QrScreenPreview(
    @PreviewParameter(MainUiStateProvider::class) uiState: MainUiState
) {
    QrScreen(
        uiState = uiState,
        onRefresh = {},
        resumeTimer = {},
        stopTimer = {})
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

@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    val window = (context as? Activity)?.window ?: return

    val isInspection = LocalInspectionMode.current
    if (isInspection) return

    DisposableEffect(Unit) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}