package minmul.kwpass.ui.landing

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import minmul.kwpass.R
import minmul.kwpass.ui.components.GentleHorizontalDivider
import minmul.kwpass.ui.components.IntroduceSection
import minmul.kwpass.ui.theme.KWPassTheme

@Composable
fun IntroduceWatchScreen(
    onNextClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.introduce_watch_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        IntroduceSection(
            stringResource = R.string.introduce_watch_app,
            painterResource = R.drawable.image_placeholder,
        )
        Spacer(modifier = Modifier.height(12.dp))
        GentleHorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))
        IntroduceSection(
            stringResource = R.string.introduce_watch_complication,
            painterResource = R.drawable.image_placeholder,
        )
        Button(
            onClick = onNextClicked,
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Text(text = stringResource(R.string.next))
        }
    }
}

@Preview
@Composable
fun IntroduceWatchScreenPreview() {
    KWPassTheme {
        IntroduceWatchScreen(onNextClicked = {})
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DarkIntroduceWatchScreenPreview() {
    KWPassTheme {
        IntroduceWatchScreen(onNextClicked = {})
    }
}