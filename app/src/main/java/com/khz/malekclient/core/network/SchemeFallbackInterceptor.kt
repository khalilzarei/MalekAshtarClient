package com.khz.malekclient.core.network

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * پشتیبانی از سرور بدون SSL + چک واقعی دسترسی با endpoint پینگ.
 *
 * رفتار:
 *  1) درخواست‌ها عادی با https می‌روند.
 *  2) اگر https در سطح اتصال شکست خورد (سرور گواهی SSL ندارد،
 *     handshake خراب است یا پورت ۴۴۳ فیلتر شده):
 *      - از endpoint پینگ روی http (با timeout کوتاه) چک می‌کند که
 *        سرور اصلاً در دسترس است یا نه.
 *      - اگر پینگ جواب داد، تصمیم «فقط http» برای کل سشن ذخیره می‌شود و
 *        همین درخواست (و تمام درخواست‌های بعدی) با http انجام می‌شود.
 *      - اگر پینگ جواب نداد، خطای اصلی برمی‌گردد (درست می‌گفتیم: وصل نیست).
 *
 * نکته: این اینترسپتور باید در انتها (نزدیک‌ترین به اتصال) اضافه شود
 * تا هدرهای Authorization قبلاً روی درخواست اعمال شده باشند.
 */
class SchemeFallbackInterceptor(
    /** آدرس پینگ روی http — مثلاً http://host/api/v1/ping */
    private val httpPingUrl: String
) : Interceptor {

    /**
     * تصمیم ذخیره‌شده: سرور فقط با http در دسترس است.
     * بعد از اولین موفقیت، دیگر هر بار تلاش https + پینگ تکرار نمی‌شود.
     */
    @Volatile
    private var forceHttp = false

    /** کلاینت جداگانه با timeout کوتاه مخصوص چک پینگ */
    private val probeClient = OkHttpClient.Builder()
        .connectTimeout(
            4,
            TimeUnit.SECONDS
        )
        .readTimeout(
            8,
            TimeUnit.SECONDS
        )
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url

        // اگر تصمیم‌گیری شده که سرور فقط با http جواب می‌دهد، از همان ابتدا http
        if (url.scheme == "https" && forceHttp) {
            return chain.proceed(
                original.newBuilder()
                    .url(httpVersionOf(url))
                    .build()
            )
        }

        // اگر خودش http است، فقط عبور می‌دهیم
        if (url.scheme != "https") {
            return chain.proceed(original)
        }

        return try {
            chain.proceed(original)
        } catch (e: IOException) {
            // فقط خطاهای سطح اتصال منجر به چک http می‌شوند
            if (!isConnectionLevelFailure(e)) {
                throw e
            }

            /*
             * https شکست خورد. اول با پینگ چک می‌کنیم که سرور روی http
             * اصلاً در دسترس است یا نه؛ وگرنه http را هم نمی‌آزماییم.
             */
            if (!probeHttp()) {
                throw e
            }

            forceHttp = true
            return try {
                chain.proceed(
                    original.newBuilder()
                        .url(httpVersionOf(url))
                        .build()
                )
            } catch (_: IOException) {
                throw e
            }
        }
    }

    /**
     * آیا سرور روی http (آدرس پینگ) جواب می‌دهد؟
     * هر پاسخ HTTP (حتی 4xx/5xx) یعنی سرور زنده است.
     */
    private fun probeHttp(): Boolean {
        val pingRequest = Request.Builder()
            .url(httpPingUrl)
            .method(
                "GET",
                null
            )
            .build()
        return try {
            probeClient.newCall(pingRequest)
                .execute()
                .use {
                    true
                }
        } catch (_: IOException) {
            false
        }
    }

    /** آدرس https را با همان host/path/params ولی اسکیم http برمی‌گرداند */
    private fun httpVersionOf(url: HttpUrl): HttpUrl {
        return url.newBuilder()
            .scheme("http")
            .build()
    }

    private fun isConnectionLevelFailure(e: Throwable): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            when (cause) {
                is SSLException           -> return true
                is CertificateException   -> return true
                is ConnectException       -> return true
                is SocketTimeoutException -> return true
            }
            cause = cause.cause
        }
        // هر IOException دیگر در مرحله‌ی اتصال (reset، unreachable و...)
        return e is IOException
    }
}
