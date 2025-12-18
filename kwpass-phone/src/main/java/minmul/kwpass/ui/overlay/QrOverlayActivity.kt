package minmul.kwpass.ui.overlay

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import minmul.kwpass.ui.main.MainViewModel
import minmul.kwpass.ui.theme.KWPassTheme

@AndroidEntryPoint
class QrOverlayActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KWPassTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val scaledQrBitmap = remember(uiState.qrBitmap) {
                    uiState.qrBitmap?.let { createNonFilteredBitmap(it, 400) }
                }

                LaunchedEffect(Unit) {
                    viewModel.refreshQR()
                }

                // 전체 화면 반투명 박스
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { finish() },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.fetchingData) {
                        CircularProgressIndicator(color = Color.White)
                    } else if (scaledQrBitmap != null) {
                        KeepScreenMaxBrightness()
                        Image(
                            bitmap = scaledQrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(250.dp)
                                .clip(RoundedCornerShape(16.dp))

                        )
                    } else if (uiState.failedToGetQr) {
                        Text(
                            text = "ERROR"
                        )
                    } else {
                        Text(
                            text = "Loading"
                        )
                    }

                    Text(
                        text = "Double tap to exit",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 120.dp),
                        color = Color.White,
                    )
                }
            }
        }
    }

    private fun createNonFilteredBitmap(source: Bitmap, size: Int): Bitmap {
        val dest = createBitmap(size, size)
        val canvas = Canvas(dest)
        val paint = Paint().apply {
            isFilterBitmap = false
            isAntiAlias = false
        }

        val srcRect = android.graphics.Rect(0, 0, source.width, source.height)
        val destRect = android.graphics.Rect(0, 0, size, size)

        canvas.drawBitmap(source, srcRect, destRect, paint)
        return dest
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