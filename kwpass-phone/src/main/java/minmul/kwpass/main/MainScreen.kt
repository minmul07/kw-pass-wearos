package minmul.kwpass.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel(), modifier: Modifier = Modifier) {
    Text(
        text = "Hello World!",
        modifier = modifier.fillMaxSize()
    )
}