package com.telepass.glancesample.stateDefinition

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.telepass.glancesample.model.ImageState
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object ImageStateDefinition : GlanceStateDefinition<ImageState> {

    private const val DATA_STORE_FILENAME = "imageState"

    private val Context.datastore by dataStore(DATA_STORE_FILENAME, ImageStateSerializer)
    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<ImageState> {
        return context.datastore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(DATA_STORE_FILENAME)
    }

    object ImageStateSerializer : Serializer<ImageState> {
        override val defaultValue = ImageState.Loading

        override suspend fun readFrom(input: InputStream): ImageState = try {
            Json.decodeFromString(
                ImageState.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Could not read data: ${exception.message}")
        }

        override suspend fun writeTo(t: ImageState, output: OutputStream) {
            output.use {
                it.write(
                    Json.encodeToString(ImageState.serializer(), t).encodeToByteArray()
                )
            }
        }
    }
}
