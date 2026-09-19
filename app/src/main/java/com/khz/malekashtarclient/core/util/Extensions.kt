package com.khz.malekashtarclient.core.util

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khz.malekashtarclient.FootballSchoolApp
import com.khz.malekashtarclient.core.di.AppContainer

/** دسترسی سریع به container از Composable */
val LocalAppContainer: AppContainer
    @Composable get() =
        (LocalContext.current.applicationContext as FootballSchoolApp).container

/**
 * ساخت ViewModel با استفاده از ViewModelFactory برنامه
 * استفاده: val vm: MyViewModel = appViewModel()
 */
@Composable
inline fun <reified T : ViewModel> appViewModel(): T {
    val app = LocalContext.current.applicationContext as FootballSchoolApp
    val factory = object : ViewModelProvider.Factory {
        override fun <V : ViewModel> create(modelClass: Class<V>): V {
            @Suppress("UNCHECKED_CAST")
            return app.container.viewModelFactory.create(modelClass) as V
        }
    }
    return viewModel(factory = factory)
}

/** ارقام فارسی */
fun String.toPersianDigits(): String {
    val english = '0'..'9'
    val persian = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val builder = StringBuilder()
    for (c in this) {
        if (c in english) {
            builder.append(persian[c - '0'])
        } else {
            builder.append(c)
        }
    }
    return builder.toString()
}

/** تبدیل ارقام انگلیسی یک عدد به فارسی */
fun Int.toPersianDigits(): String = this.toString().toPersianDigits()
fun Long.toPersianDigits(): String = this.toString().toPersianDigits()

/** جداکننده‌ی هزارگان با رقم فارسی (مثلاً 1500000 → ۱٬۵۰۰٬۰۰۰) */
fun Long.formatThousands(): String {
    val s = this.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in s.indices.reversed()) {
        sb.append(s[i])
        count++
        if (count == 3 && i != 0) {
            sb.append('٬')
            count = 0
        }
    }
    return sb.reverse().toString().toPersianDigits()
}

/** تبدیل امن به Int (پیش‌فرض 0) */
fun Any?.toIntSafe(default: Int = 0): Int = when (this) {
    is Int -> this
    is Long -> this.toInt()
    is String -> this.toIntOrNull() ?: default
    is Number -> this.toInt()
    else -> default
}

/** تبدیل امن به Long (پیش‌فرض 0) */
fun Any?.toLongSafe(default: Long = 0L): Long = when (this) {
    is Long -> this
    is Int -> this.toLong()
    is String -> this.toLongOrNull() ?: default
    is Number -> this.toLong()
    else -> default
}

/** تبدیل امن به Double (پیش‌فرض 0.0) */
fun Any?.toDoubleSafe(default: Double = 0.0): Double = when (this) {
    is Double -> this
    is Float -> this.toDouble()
    is Int -> this.toDouble()
    is Long -> this.toDouble()
    is String -> this.toDoubleOrNull() ?: default
    is Number -> this.toDouble()
    else -> default
}
