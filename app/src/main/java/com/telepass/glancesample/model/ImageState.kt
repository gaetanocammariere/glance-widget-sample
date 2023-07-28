package com.telepass.glancesample.model
import kotlinx.serialization.Serializable
@Serializable
sealed interface ImageState {
    @Serializable
    data object Loading : ImageState

    @Serializable
    data class Success(val url : String) : ImageState
}