package minmul.kwpass.service

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import minmul.kwpass.ui.widget.KwPassWidget

class KwPassWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KwPassWidget()
}

