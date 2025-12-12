package minmul.kwpass.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import minmul.kwpass.R

@Composable
fun IntroduceSection(
    stringResource: Int, painterResource: Int,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(painterResource),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(stringResource),
            modifier = Modifier.padding(horizontal = 8.dp),
            style = style
        )

    }
}

@Preview(widthDp = 400)
@Composable
fun IntroduceSectionPreview1() {
    IntroduceSection(
        stringResource = R.string.introduce_phone_widget,
        painterResource = R.drawable.image_placeholder
    )
}