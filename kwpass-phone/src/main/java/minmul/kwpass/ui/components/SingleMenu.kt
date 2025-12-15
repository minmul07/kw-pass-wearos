package minmul.kwpass.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import minmul.kwpass.ui.main.conditional
import minmul.kwpass.ui.theme.KWPassTheme

@Composable
fun SingleMenu(
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    title: String,
    subTitle: String? = null,
    top: Boolean = true,
    bottom: Boolean = true,
    onclick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null
) {
    if (top) {
        Spacer(modifier = Modifier.height(8.dp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .conditional(onclick != null) {
                clickable { onclick?.invoke() }
            },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.inverseOnSurface
        ),
        shape = RoundedCornerShape(
            topStart = if (top) 24.dp else 4.dp,
            topEnd = if (top) 24.dp else 4.dp,
            bottomStart = if (bottom) 24.dp else 4.dp,
            bottomEnd = if (bottom) 24.dp else 4.dp,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(18.dp)
                        .size(36.dp)
                )
            } else if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(18.dp)
                        .size(36.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (subTitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subTitle, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(18.dp)
                        .size(18.dp)
                )
            }
        }
    }

    if (bottom) {
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Preview
@Composable
fun SingleMenuPreview() {
    KWPassTheme {
        SingleMenu(
            title = "Sample Title",
            subTitle = "Subtitle below main title.",
            imageVector = Icons.Default.Language
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