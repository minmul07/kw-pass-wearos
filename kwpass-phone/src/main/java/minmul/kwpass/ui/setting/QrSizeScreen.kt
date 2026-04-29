package minmul.kwpass.ui.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import minmul.kwpass.R
import minmul.kwpass.ui.components.KwPassTopAppBar
import minmul.kwpass.ui.components.TipBox
import minmul.kwpass.ui.main.MainUiState
import minmul.kwpass.ui.theme.KWPassTheme

private const val MinQrSize = 140f
private const val MaxQrSize = 320f
private val QrSizeSliderGap = 40.dp
private val SliderTouchTargetHeight = 48.dp

@Composable
fun QrSizeScreenTopBar(
    navigateUp: () -> Unit, modifier: Modifier = Modifier
) {
    KwPassTopAppBar(
        title = stringResource(R.string.qrcode_size),
        modifier = modifier,
        navigateUp = navigateUp,
    )
}

@Composable
fun QrSizeScreen(
    navController: NavController,
    mainUiState: MainUiState,
    onQrSizeModified: (Float) -> Unit,
    saveQrSizeOnDisk: () -> Unit
) {
    DisposableEffect(Unit) {
        onDispose {
            saveQrSizeOnDisk()
        }
    }

    Scaffold(
        topBar = {
            QrSizeScreenTopBar(
                navigateUp = { navController.navigateUp() })
        }, modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TipBox(
                title = stringResource(R.string.about_size),
                icon = Icons.Default.Info,
                text = stringResource(R.string.about_size_description),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (mainUiState.process.sampleQrBitmap != null) {
                Text(
                    text = stringResource(R.string.not_working_qr),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-(mainUiState.process.qrSize / 2)).dp - 28.dp)
                )

                Image(
                    bitmap = mainUiState.process.sampleQrBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(mainUiState.process.qrSize.dp)
                        .clip(MaterialTheme.shapes.large)
                        .zIndex(1f),
                    filterQuality = FilterQuality.None
                )
            }

            Slider(
                value = mainUiState.process.qrSize.toFloat(),
                onValueChange = { onQrSizeModified(it) },
                valueRange = MinQrSize..MaxQrSize,
                steps = 29,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        y = (MaxQrSize / 2).dp + QrSizeSliderGap + (SliderTouchTargetHeight / 2)
                    )
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )
        }
    }
}


@Preview
@Composable
fun QrSizeScreenPreview() {
    KWPassTheme {
        QrSizeScreen(
            navController = rememberNavController(),
            mainUiState = MainUiState(),
            onQrSizeModified = {},
            saveQrSizeOnDisk = {})
    }
}
