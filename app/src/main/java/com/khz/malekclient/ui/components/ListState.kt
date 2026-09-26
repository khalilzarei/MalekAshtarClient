package com.khz.malekclient.ui.components

/**
 * حالت کلی یک صفحه‌ی لیستی (Loading / Success / Error)
 *
 * امضای تغییرناپذیر طبق پرامپت
 */
sealed class ListState<out T> {
    data object Loading : ListState<Nothing>()
    data class Success<T>(val items: List<T>) : ListState<T>()
    data class Error(val message: String) : ListState<Nothing>()
}
