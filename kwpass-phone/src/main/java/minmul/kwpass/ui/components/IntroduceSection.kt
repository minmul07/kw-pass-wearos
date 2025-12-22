package minmul.kwpass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import minmul.kwpass.R

@Composable
fun IntroduceSection(
    icon: ImageVector, text: String, style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(4.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .padding(4.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = style
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IntroduceSectionPreview1() {
    IntroduceSection(
        text = stringResource(R.string.introduce_phone_widget),
        icon = Icons.Default.PhoneAndroid
    )
}