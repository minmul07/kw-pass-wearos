package minmul.kwpass.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import minmul.kwpass.R
import minmul.kwpass.ui.overlay.QrOverlayActivity
import minmul.kwpass.ui.theme.KWPassTheme

class KwPassWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            provideContent {
                GlanceTheme(colors = GlanceTheme.colors) {
                    WidgetContent(
                        context = context
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            provideContent {
                Text("ERROR")
            }
        }
    }

    @Composable
    fun WidgetContent(
        context: Context
    ) {
        val size = LocalSize.current
        val isSmall = size.width < 110.dp

        val iconSize = if (isSmall) 32.dp else 48.dp
        val textSize = if (isSmall) 10.sp else 12.sp
        val contentPadding = if (isSmall) 8.dp else 16.dp
        val spacerHeight = if (isSmall) 2.dp else 4.dp

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(
                    ColorProvider(
                        day = Color.White.copy(alpha = 0.8f),
                        night = Color.Black.copy(alpha = 0.6f)
                    )
                )
                .appWidgetBackground()
                .padding(contentPadding)
                .clickable(actionStartActivity<QrOverlayActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    provider = ImageProvider(R.drawable.qr_icon_72_white),
                    contentDescription = null,
                    modifier = GlanceModifier.size(iconSize),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                )
                Spacer(modifier = GlanceModifier.height(spacerHeight))
                Text(
                    text = context.getString(R.string.widget_title),
                    style = TextStyle(
                        color = ColorProvider(day = Color.Black, night = Color.White),
                        fontSize = textSize,
                        textAlign = TextAlign.Center
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
fun KwPassWidgetPreview() {
    KWPassTheme {
        KwPassWidget().WidgetContent(
            context = LocalContext.current
        )
    }
}