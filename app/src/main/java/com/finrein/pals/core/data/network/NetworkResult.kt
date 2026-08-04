package com.finrein.pals.core.data.network

/**
 * A sealed interface representing network operational states and data delivery.
 * Encapsulates response data, exceptions, HTTP status codes, and loading states
 * to shield higher architecture layers (Domain/MVVM) from transport framework specifics.
 */
sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Error(val exception: Throwable, val statusCode: Int? = null) : NetworkResult<Nothing>
    object Loading : NetworkResult<Nothing>
}

/**
 * Transforms the inner payload of a [NetworkResult.Success] while preserving errors and loading state.
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> NetworkResult.Error(exception, statusCode)
        NetworkResult.Loading -> NetworkResult.Loading
    }
}

/**
 * Safely converts a standard Kotlin [Result] into a clean [NetworkResult].
 */
fun <T> Result<T>.toNetworkResult(): NetworkResult<T> {
    return fold(
        onSuccess = { NetworkResult.Success(it) },
        onFailure = { NetworkResult.Error(it) }
    )
}
