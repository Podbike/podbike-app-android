package com.podbike.app.utils

import timber.log.Timber

/**
 * Executes [block] in a safe manner, handling exception as [ResourceError].
 * In case of an error, returns a result of the [onErrorReturn] method.
 * - It is possible to catch an error by providing implementation of the [onError] method.
 */
internal inline fun <T> runWithErrorHandling(
    onErrorReturn: (Throwable) -> Result<T, Throwable> = { Result.Error(it) },
    onError: (Throwable) -> Unit = {},
    block: () -> T
): Result<T, Throwable> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Timber.e("Exception! $e")
        onError(e)
        onErrorReturn(e)
    }
}

private typealias RootError = Throwable
typealias DataResult<T> = Result<T, Throwable>
typealias DataResultError<T> = Result.Error<T, Throwable>

sealed interface Result<out T, out E : RootError> {
    data class Success<out T>(val data: T) : Result<T, Nothing>
    data class Error<out T, out E : RootError>(val error: E, val data: T? = null) : Result<T, E>
}

fun <T, R> DataResult<T>.mapTo(block: (T?) -> R?): DataResult<R?> {
    return when (this) {
        is Result.Success -> Result.Success(block(this.data))
        is Result.Error -> Result.Error(error, block(this.data))
    }
}

