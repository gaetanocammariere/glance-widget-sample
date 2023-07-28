package com.telepass.glancesample.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.telepass.glancesample.widget.ImageWidget

class GlanceSampleReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = ImageWidget()
}
