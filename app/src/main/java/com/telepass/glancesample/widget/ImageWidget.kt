package com.telepass.glancesample.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.ImageProvider
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontStyle
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.telepass.glancesample.model.ImageState
import com.telepass.glancesample.stateDefinition.ImageStateDefinition
import com.telepass.glancesample.toPx
import com.telepass.glancesample.worker.ImageWorker

class ImageWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition = ImageStateDefinition


    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        val size = LocalSize.current
        val context = LocalContext.current
        val glanceId = LocalGlanceId.current
        val imageState = currentState<ImageState>()
        GlanceTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(GlanceTheme.colors.background)
                    .cornerRadius(16.dp),
                contentAlignment = getBoxAlignment(imageState)
            ) {
                when (imageState) {
                    ImageState.Loading -> LoadingState()
                    is ImageState.Success -> SuccessState(path = imageState.url)
                }

                LaunchedEffect(Unit){
                    ImageWorker.enqueue(context, size, glanceId)
                }
            }
        }
    }

    private fun getBoxAlignment(imageState: ImageState) = when (imageState) {
            ImageState.Loading -> Alignment.Center
            is ImageState.Success -> Alignment.BottomEnd
    }

    @Composable
    private fun SuccessState(path: String) {
        Image(
            provider = getImageProvider(path),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(actionRunCallback<RefreshAction>())
        )
        Text(
            text = "Tap to refresh",
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.End,
                textDecoration = TextDecoration.Underline
            ),
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(GlanceTheme.colors.surface)
        )
    }

    @Composable
    private fun LoadingState() {
        CircularProgressIndicator()
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        ImageWorker.cancel(context, glanceId)
    }

    private fun getImageProvider(path: String): ImageProvider {
        if (path.startsWith("content://")) {
            return ImageProvider(path.toUri())
        }
        val bitmap = BitmapFactory.decodeFile(path)
        return ImageProvider(bitmap)
    }
}


