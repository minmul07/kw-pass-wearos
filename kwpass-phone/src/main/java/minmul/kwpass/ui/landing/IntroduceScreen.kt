package minmul.kwpass.ui.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Watch
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
fun IntroduceScreen(
    onNextClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = stringResource(R.string.introduce_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.75f)
                .padding(horizontal = 16.dp)
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                IntroduceSection(
                    text = stringResource(R.string.introduce_phone_widget),
                    icon = Icons.Default.PhoneAndroid
                )

                GentleHorizontalDivider(thickness = 2)

                IntroduceSection(
                    text = stringResource(R.string.introduce_phone_static_shortcuts),
                    icon = Icons.Default.AutoMode
                )

                GentleHorizontalDivider(thickness = 2)

                IntroduceSection(
                    text = stringResource(R.string.introduce_watch_app), icon = Icons.Default.Watch
                )

                GentleHorizontalDivider(thickness = 2)

                IntroduceSection(
                    text = stringResource(R.string.introduce_watch_complication),
                    icon = Icons.Default.Watch
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onNextClicked, modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(text = stringResource(R.string.next))
                    }
                }
            }
        }

    }
}


@Preview(
    showSystemUi = true
)
@Composable
fun IntroduceScreenPreview() {
    KWPassTheme {
        IntroduceScreen(
            onNextClicked = {})
    }
}