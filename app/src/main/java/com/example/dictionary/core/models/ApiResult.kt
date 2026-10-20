package com.example.dictionary.core.models

sealed class ApiResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : ApiResult<T>(data = data)

    class Failure<T>(message: String) : ApiResult<T>(message = message)
}
