package com.khz.malekclient.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * اگر اینترنت وصل نباشد یا فیلترشکن روشن باشد، درخواست را
 * بلافاصله با خطای مناسب رد می‌کند (به‌جای صبر برای timeout ۳۰ ثانیه‌ای).
 */
class ConnectivityGuardInterceptor(
    private val monitor: ConnectivityMonitor
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val state = monitor.current()
        val message = ConnectivityMonitor.messageFor(state)
        if (message != null) {
            throw NetworkConnectivityException(message)
        }
        return chain.proceed(chain.request())
    }
}
