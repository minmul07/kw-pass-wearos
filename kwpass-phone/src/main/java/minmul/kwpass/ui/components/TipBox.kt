package minmul.kwpass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import minmul.kwpass.ui.theme.KWPassTheme

@Composable
fun TipBox(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    text: String
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null || title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )

                    }
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSecondaryContainer
            )
        }
    }
}


@Preview
@Composable
fun TipBoxPreview() {
    KWPassTheme {
        TipBox(
            title = "Sample Title",
            icon = Icons.Default.Info,
            text = "Gemini gives you a personalized experience using your past chats. You can also give it instructions to customize its responses. "
        )
    }
}

@Preview
@Composable
fun TipBoxNoTitlePreview() {
    KWPassTheme {
        TipBox(
            text = "Gemini gives you a personalized experience using your past chats. You can also give it instructions to customize its responses. "
        )
    }
}
