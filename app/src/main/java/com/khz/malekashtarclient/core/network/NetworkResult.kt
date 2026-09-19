package com.khz.malekashtarclient.core.network

/**
 * نتیجه‌ی یک فراخوانی شبکه‌ای که در همه‌ی ریپازیتوری‌ها استفاده می‌شود.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}

/** میان‌بر برای استخراج داده یا null */
fun <T> NetworkResult<T>.dataOrNull(): T? = (this as? NetworkResult.Success<T>)?.data

/** میان‌بر برای استخراج پیام خطا یا null */
fun NetworkResult<*>.errorMessageOrNull(): String? =
    (this as? NetworkResult.Error)?.message
