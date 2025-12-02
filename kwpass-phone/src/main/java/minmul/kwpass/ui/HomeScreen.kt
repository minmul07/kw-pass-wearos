package minmul.kwpass.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import minmul.kwpass.R
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.main.MainViewModel
import minmul.kwpass.service.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenAppBar(
    navigateSetting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        actions = {
            IconButton(
                onClick = navigateSetting
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.setting)
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = colorScheme.primaryContainer
        ),
        modifier = modifier,

        )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier, mainViewModel: MainViewModel, navController: NavController
) {

    Scaffold(
        topBar = {
            HomeScreenAppBar(
                navigateSetting = {
                    navController.navigate(ScreenDestination.Setting)
                },
                modifier = Modifier
            )
        }

    ) { paddingValues ->
        Column(
            modifier = modifier.padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = "학번: ${mainViewModel.rid}")
                    Text(text = "비밀번호: ${mainViewModel.password}")
                    Text(text = "전화번호: ${mainViewModel.tel}")
                    Text(
                        text = "QR: ${mainViewModel.qr.replace("    ", " ")}",
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = {
                    mainViewModel.refreshQR()
                },
                enabled = mainViewModel.validation && !mainViewModel.fetchingData
            ) {
                if (!mainViewModel.fetchingData) {
                    Text(text = stringResource(R.string.fetch))
                } else {
                    Text(text = stringResource(R.string.checking))
                }
            }
        }
    }
}

//@SuppressLint("ViewModelConstructorInComposable")
//@Preview
//@Composable
//fun HomeScreenPreview() {
//    HomeScreen(
//        modifier = Modifier,
//        mainViewModel = MainViewModel(userData = UserData(LocalContext.current)),
//        navController = rememberNavController()
//    )
//}