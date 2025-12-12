package minmul.kwpass.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun GentleHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Int = 4,
    color: Color = Color.LightGray,
) {
    HorizontalDivider(
        thickness = thickness.dp,
        color = color,
        modifier = modifier.clip(RoundedCornerShape((thickness / 2).dp))
    )
}

@Preview
@Composable
fun GentleHorizontalDividerPreview() {
    GentleHorizontalDivider()
}