package com.khz.malekashtarclient.core.network

import com.khz.malekashtarclient.core.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * افزودن هدر Authorization: Bearer <token> به همه‌ی درخواست‌ها.
 *
 * نکته: SessionManager مبتنی بر Flow است؛ برای خواندن در Interceptor
 * (که خارج از suspend context اجرا می‌شود) از runBlocking استفاده می‌کنیم.
 * این الگو در اپ ادمین هم استفاده شده و در عمل overhead ناچیزی دارد.
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = runBlocking {
            sessionManager.getTokenSync()
        }

        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            original.newBuilder()
                .header("Accept", "application/json")
                .build()
        }

        return chain.proceed(request)
    }
}
