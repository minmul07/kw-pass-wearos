package minmul.kwpass.ui

import android.os.Build
import android.view.RoundedCorner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberDeviceCornerRadius(): Dp {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current

    return remember(context, view, density) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val insets = view.rootWindowInsets
            val topLeft = insets?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
            val topRight = insets?.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)
            val bottomLeft = insets?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)
            val bottomRight = insets?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)

            val radiusPx =
                topLeft?.radius ?: topRight?.radius ?: bottomLeft?.radius ?: bottomRight?.radius
                ?: 0

            with(density) { radiusPx.toDp() }
        } else {
            0.dp // 구형 안드로이드 및 사각 화면 (곡률 알 수 없음)
        }
    }
}

@Composable
fun RoundedClippingScreenWarper(
    animationDuration: Long = 400L,
    content: @Composable () -> Unit
) {
    val cornerRadius = rememberDeviceCornerRadius()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // 기본값 true (진행 중인 애니메이션 존재 가능)
    var isClippingEnabled by remember { mutableStateOf(true) }
    var toggleJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // 1. 화면 진입, B->A 복귀, 제스처 취소
                Lifecycle.Event.ON_RESUME -> {
                    isClippingEnabled = true
                    toggleJob?.cancel()

                    // 애니메이션 시간만큼 기다렸다가 클리핑 해제
                    toggleJob = scope.launch {
                        delay(animationDuration)
                        isClippingEnabled = false
                    }
                }

                // 2. 화면 이탈, 뒤로가기 제스처 시작
                Lifecycle.Event.ON_PAUSE -> {
                    // 타이머 중지
                    toggleJob?.cancel()

                    // 즉시 둥글게 처리하여 축소 애니메이션 준비
                    isClippingEnabled = true
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            toggleJob?.cancel() // 컴포저블 해제 시 타이머 정리
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(
                if (isClippingEnabled && cornerRadius > 0.dp) RoundedCornerShape(cornerRadius)
                else RectangleShape
            )
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}