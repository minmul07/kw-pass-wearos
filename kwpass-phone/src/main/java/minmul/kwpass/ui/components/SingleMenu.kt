package minmul.kwpass.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import minmul.kwpass.ui.theme.KWPassTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleMenu(
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    title: String,
    subTitle: String? = null,
    top: Boolean = true,
    bottom: Boolean = true,
    onclick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    iconTint: Color? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (top) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        val cardModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
        val cardColors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainer,
            contentColor = colorScheme.onSurface,
        )
        val cardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        val cardShape = RoundedCornerShape(
            topStart = if (top) 24.dp else 6.dp,
            topEnd = if (top) 24.dp else 6.dp,
            bottomStart = if (bottom) 24.dp else 6.dp,
            bottomEnd = if (bottom) 24.dp else 6.dp,
        )

        if (onclick != null) {
            Card(
                onClick = onclick,
                modifier = cardModifier,
                colors = cardColors,
                elevation = cardElevation,
                shape = cardShape,
            ) {
                SingleMenuContent(
                    imageVector = imageVector,
                    painter = painter,
                    title = title,
                    subTitle = subTitle,
                    trailingIcon = trailingIcon,
                    iconTint = iconTint,
                )
            }
        } else {
            Card(
                modifier = cardModifier,
                colors = cardColors,
                elevation = cardElevation,
                shape = cardShape,
            ) {
                SingleMenuContent(
                    imageVector = imageVector,
                    painter = painter,
                    title = title,
                    subTitle = subTitle,
                    trailingIcon = trailingIcon,
                    iconTint = iconTint,
                )
            }
        }

        if (bottom) {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SingleMenuContent(
    imageVector: ImageVector?,
    painter: Painter?,
    title: String,
    subTitle: String?,
    trailingIcon: ImageVector?,
    iconTint: Color?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .sizeIn(minHeight = 48.dp)
            .padding(start = 18.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconPainter = imageVector?.let { rememberVectorPainter(it) } ?: painter

        if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = iconTint
                    ?: if (imageVector != null) colorScheme.primary else Color.Unspecified,
                modifier = Modifier.size(30.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (iconPainter != null) 18.dp else 0.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subTitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
            )
        }
    }
}

@Preview
@Composable
fun SingleMenuPreview() {
    KWPassTheme {
        SingleMenu(
            title = "Sample Title",
            subTitle = "Subtitle below main title.",
        )
    }
}

@Preview
@Composable
fun SingleMenuPreview2() {
    KWPassTheme {
        SingleMenu(
            title = "Sample Title",
            subTitle = "Subtitle below main title.",
            imageVector = Icons.Default.Language,
            top = false
        )
    }
}

@Preview
@Composable
fun SingleMenuPreview3() {
    KWPassTheme {
        SingleMenu(
            title = "Sample Title",
            subTitle = "Subtitle below main title.",
            imageVector = Icons.Default.Language,
            bottom = false,
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForwardIos
        )
    }
}

@Preview
@Composable
fun SingleMenuPreview4() {
    KWPassTheme {
        Column {
            SingleMenu(
                title = "Sample Title",
                subTitle = "Subtitle below main title.",
                imageVector = Icons.Default.Language,
                bottom = false
            )
            SingleMenu(
                title = "Sample Title",
                subTitle = "Subtitle below main title.",
                imageVector = Icons.Default.Language,
                top = false
            )
        }
    }
}
