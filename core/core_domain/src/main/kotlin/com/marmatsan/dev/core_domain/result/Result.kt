package com.marmatsan.dev.core_domain.result

typealias RootError = Error

sealed class Result<out D, out E : RootError> {
    data class Success<out D, out E : RootError>(val data: D? = null) : Result<D, E>()
    data class Error<out D, out E : RootError>(val error: E? = null) : Result<D, E>()

    inline fun fold(
        onError: (E?) -> Unit,
        onSuccess: (D?) -> Unit
    ) = when (this) {
        is Error -> onError(error)
        is Success -> onSuccess(data)
    }
}