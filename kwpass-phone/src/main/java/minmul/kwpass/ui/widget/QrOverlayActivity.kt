package minmul.kwpass.ui.widget

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import minmul.kwpass.R
import minmul.kwpass.ui.main.MainViewModel
import minmul.kwpass.ui.theme.KWPassTheme
import kotlin.math.hypot

@AndroidEntryPoint
class QrOverlayActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)

        viewModel.refreshQR()
    }

    override fun onDestroy() {
        super.onDestroy()


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KWPassTheme {
                val context = LocalContext.current
                LaunchedEffect(key1 = true) {
                    viewModel.toastEvent.collect { uiText ->
                        val message = uiText.asString(context)
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

                val uiState by viewModel.mainUiState.collectAsStateWithLifecycle()

                val lifecycleOwner = LocalLifecycleOwner.current

                val density = LocalDensity.current
                val scope = rememberCoroutineScope() // 애니메이션 실행용 스코프

                val offsetX = remember { Animatable(0f, Float.VectorConverter) }
                val offsetY = remember { Animatable(0f, Float.VectorConverter) }

                val exitDistanceThreshold = with(density) { 100.dp.toPx() }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Event.ON_RESUME) {
                            if (uiState.accountInfo.hasValidInfo) {
                                viewModel.refreshQR()
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
                            viewModel.refreshQR()
                        }
                    }
                }

                LaunchedEffect(uiState.accountInfo.hasValidInfo) {
                    if (uiState.accountInfo.hasValidInfo) {
                        viewModel.refreshQR()
                    }
                }

                // 전체 화면 반투명 박스
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    scope.launch {
                                        offsetX.snapTo(offsetX.value + dragAmount.x)
                                        offsetY.snapTo(offsetY.value + dragAmount.y)
                                    }
                                },
                                onDragEnd = {
                                    val distance = hypot(offsetX.value, offsetY.value)

                                    if (distance > exitDistanceThreshold) {
                                        finish()
                                    } else {
                                        scope.launch {
                                            offsetX.animateTo(0f)
                                            offsetY.animateTo(0f)
                                        }
                                    }

                                }
                            )
                        }
                        .clickable(
                            onClick = {
                                if (viewModel.backAction()) {
                                    finish()
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.process.isFetching) {
                        CircularProgressIndicator(color = Color.White)
                    } else if (uiState.process.qrBitmap != null) {
                        KeepScreenMaxBrightness()
                        Image(
                            bitmap = uiState.process.qrBitmap!!.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(250.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            filterQuality = FilterQuality.None

                        )
                    } else if (uiState.process.fetchFailed) {
                        Text(
                            text = stringResource(R.string.error_common),
                            color = Color.White,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.fetching),
                            color = Color.White
                        )
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