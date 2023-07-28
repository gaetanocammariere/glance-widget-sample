package com.telepass.glancesample

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.graphics.fonts.FontStyle
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.unit.dp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val widgetProvider =
            AppWidgetManager.getInstance(this)
                .getInstalledProvidersForPackage(packageName, null)
                .first()



        setContent {
            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                        context
                    )
                }

                isSystemInDarkTheme() -> DarkColorScheme
                else -> LightColorScheme
            }
            MaterialTheme(colorScheme) {
                Scaffold {
                    Column(Modifier.padding(it)) {
                        WidgetSelectionCard(widgetProvider)
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun WidgetSelectionCard(providerInfo: AppWidgetProviderInfo) {
        val context = LocalContext.current
        val label = providerInfo.loadLabel(context.packageManager)
        val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            providerInfo.loadDescription(context).toString()
        } else {
            "Description not available"
        }
        val preview = painterResource(id = providerInfo.previewImage)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {
                providerInfo.attachWidget(context)
            }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier.size(256.dp),
                        painter = preview,
                        contentDescription = description
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Tap to add it to the homescreen",
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = Italic
                    )
                }

            }
        }
    }

    private fun AppWidgetProviderInfo.attachWidget(context: Context) {
        val successCallback = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, WidgetPinnedReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        AppWidgetManager.getInstance(context).requestPinAppWidget(provider, null, successCallback)
    }
}