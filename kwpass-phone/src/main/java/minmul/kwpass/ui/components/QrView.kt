package minmul.kwpass.ui.components

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun QrView(
    isFetching: Boolean,
    unavailable: Boolean,
    qrBitmap: Bitmap?,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(qrBitmap) {
        if (qrBitmap != null) {
            while (isActive) {
                delay(50000L)
                onClick()
            }
        }
    }
    val animatedQrBlur: Dp by animateDpAsState(
        if (isFetching || unavailable) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 200)
    )

    val animatedQrSize by animateDpAsState(
        targetValue = if (isFetching || unavailable) 234.dp else 256.dp,
        animationSpec = tween(durationMillis = 400)
    )


    val qrAlpha by animateFloatAsState(
        targetValue = if (isFetching || unavailable) 0.5f else 1.0f,
        animationSpec = tween(durationMillis = 200)
    )

    val errorIconAlpha by animateFloatAsState(
        targetValue = if (unavailable) 0f else 1f, animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = Modifier
            .size(animatedQrSize)
            .clickable(
                onClick = onClick, indication = null, interactionSource = interactionSource
            ), contentAlignment = Alignment.Center
    ) {

        if (unavailable) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(errorIconAlpha)
            )
        }


        AnimatedVisibility(qrBitmap != null, enter = fadeIn(), exit = fadeOut()) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(qrAlpha)
                        .clip(RoundedCornerShape(16.dp))
                        .blur(
                            radius = animatedQrBlur, edgeTreatment = BlurredEdgeTreatment.Unbounded
                        )
                        .zIndex(1f),
                    filterQuality = FilterQuality.None
                )
            }

            if (!isFetching && !unavailable && qrBitmap != null) {
                KeepScreenMaxBrightness()
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