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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun QrView(
    isFetching: Boolean,
    unavailable: Boolean,
    qrBitmap: Bitmap?,
    refresh: () -> Unit,
    qrSize: Int,
    modifier: Modifier = Modifier,
    contentDescription: String = "QR코드"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isQrDimmed = isFetching || unavailable

    val animatedQrBlur: Dp by animateDpAsState(
        if (isQrDimmed) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 200)
    )


    val shrunkQrSize: Int = (qrSize * 0.9375).toInt()
    val animatedQrSize by animateDpAsState(
        targetValue = if (isQrDimmed) shrunkQrSize.dp else qrSize.dp,
        animationSpec = tween(durationMillis = 400)
    )

    val qrAlpha by animateFloatAsState(
        targetValue = if (isQrDimmed) 0.5f else 1.0f,
        animationSpec = tween(durationMillis = 200)
    )

    val errorIconAlpha by animateFloatAsState(
        targetValue = if (unavailable) 1f else 0f,
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = modifier
            .size(animatedQrSize)
            .clickable(
                onClick = refresh,
                indication = null,
                interactionSource = interactionSource,
                enabled = !isFetching,
                role = Role.Button
            ), contentAlignment = Alignment.Center
    ) {

        if (unavailable) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(errorIconAlpha)
                    .zIndex(2f)
            )
        }


        AnimatedVisibility(qrBitmap != null, enter = fadeIn(), exit = fadeOut()) {
            val visibleQrBitmap = qrBitmap

            if (visibleQrBitmap != null) {
                Image(
                    bitmap = visibleQrBitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(qrAlpha)
                        .clip(MaterialTheme.shapes.large)
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

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val originalBrightness = window.attributes.screenBrightness

        val observer = LifecycleEventObserver { _, event ->
            val layoutParams = window.attributes

            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    layoutParams.screenBrightness = 1f
                    window.attributes = layoutParams
                }

                Lifecycle.Event.ON_PAUSE -> {
                    layoutParams.screenBrightness = originalBrightness
                    window.attributes = layoutParams
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)

            val layoutParams = window.attributes
            layoutParams.screenBrightness = originalBrightness
            window.attributes = layoutParams
        }
    }
}
