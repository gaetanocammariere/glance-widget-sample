package com.telepass.glancesample.worker

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.ui.unit.DpSize
import androidx.core.content.FileProvider.getUriForFile
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.telepass.glancesample.model.ImageState
import com.telepass.glancesample.stateDefinition.ImageStateDefinition
import com.telepass.glancesample.toPx
import com.telepass.glancesample.widget.ImageWidget
import java.time.Duration
import kotlin.math.roundToInt


class ImageWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {

        private const val WIDTH_KEY = "width"
        private const val HEIGHT_KEY = "height"
        private const val FORCE_KEY = "force"

        private val uniqueWorkName = ImageWorker::class.java.simpleName

        fun enqueue(context: Context, size: DpSize, glanceId: GlanceId, force: Boolean = false) {
            val manager = WorkManager.getInstance(context)
            val requestBuilder = OneTimeWorkRequestBuilder<ImageWorker>().apply {
                addTag(glanceId.toString())
                setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                setInputData(
                    Data.Builder()
                        .putFloat(WIDTH_KEY, size.width.value.toPx())
                        .putFloat(HEIGHT_KEY, size.height.value.toPx())
                        .putBoolean(FORCE_KEY, force)
                        .build()
                )
            }
            val workPolicy = if (force) {
                ExistingWorkPolicy.REPLACE
            } else {
                ExistingWorkPolicy.KEEP
            }

            manager.enqueueUniqueWork(
                uniqueWorkName + size.width + size.height,
                workPolicy,
                requestBuilder.build()
            )
        }


        fun cancel(context: Context, glanceId: GlanceId) {
            WorkManager.getInstance(context).cancelAllWorkByTag(glanceId.toString())
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val width = inputData.getFloat(WIDTH_KEY, 0f)
            val height = inputData.getFloat(HEIGHT_KEY, 0f)
            val force = inputData.getBoolean(FORCE_KEY, false)
            
            setWidgetState(ImageState.Loading)
            val uri = getRandomImage(width, height, force)
            setWidgetState(ImageState.Success(uri))
            Result.success()

        } catch (e: Exception) {
            Log.e(uniqueWorkName, "Error while loading image", e)
            if (runAttemptCount < 10) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun setWidgetState(newState: ImageState) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(ImageWidget::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                definition = ImageStateDefinition,
                glanceId = glanceId,
                updateState = { newState }
            )
        }
        ImageWidget().updateAll(context)
    }

    /**
     * Use Coil and Picsum Photos to randomly load images into the cache based on the provided
     * size. This method returns the path of the cached image, which you can send to the widget.
     */
    @OptIn(ExperimentalCoilApi::class)
    private suspend fun getRandomImage(width: Float, height: Float, force: Boolean): String {
        val url = "https://picsum.photos/${width.roundToInt()}/${height.roundToInt()}"
        val request = ImageRequest.Builder(context)
            .data(url)
            .build()

        // Request the image to be loaded and throw error if it failed
        with(context.imageLoader) {
            if (force) {
                diskCache?.remove(url)
                memoryCache?.remove(MemoryCache.Key(url))
            }
            val result = execute(request)
            if (result is ErrorResult) {
                throw result.throwable
            }
        }

        // Get the path of the loaded image from DiskCache.
        val path = context.imageLoader.diskCache?.get(url)?.use { snapshot ->
            val imageFile = snapshot.data.toFile()

            // Use the FileProvider to create a content URI
            val contentUri = getUriForFile(
                context,
                "com.telepass.glancesample.provider",
                imageFile
            )

            // Find the current launcher everytime to ensure it has read permissions
            val resolveInfo = context.packageManager.resolveActivity(
                Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) },
                PackageManager.MATCH_DEFAULT_ONLY
            )
            val launcherName = resolveInfo?.activityInfo?.packageName
            if (launcherName != null) {
                context.grantUriPermission(
                    launcherName,
                    contentUri,
                    FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            }

            // return the path
            contentUri.toString()
        }
        return requireNotNull(path) {
            "Couldn't find cached file"
        }
    }
}