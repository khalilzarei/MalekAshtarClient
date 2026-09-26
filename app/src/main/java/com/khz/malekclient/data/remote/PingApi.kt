package com.khz.malekclient.data.remote

import com.khz.malekclient.core.network.ApiResponse
import retrofit2.http.GET

/**
 * Ping (Health Check) — سرور /api/v1/ping
 *
 * برای چک واقعیِ دسترسی به سرور (اتصال end-to-end)، جدا از
 * وضعیت شبکه‌ی سیستمی.
 */
interface PingApi {
    @GET("ping")
    suspend fun ping(): ApiResponse<String>
}
