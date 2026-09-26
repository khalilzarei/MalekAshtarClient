package com.khz.malekclient.core.network

import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * استخراج پیام خطای قابل نمایش از Exceptionهای مختلف.
 *
 * اولویت:
 * 1. اگر HttpException باشد → message از بدنه‌ی JSON پاسخ (data.message)
 * 2. اگر خطای شبکه/timeout → پیام فارسی عمومی
 * 3. در غیر این صورت → e.localizedMessage
 */
object ApiErrorHandler {

    private val gson = Gson()

    fun extractMessage(e: Throwable): String {
        return when (e) {
            // خطای وضعیت اتصال (اینترنت قطع / فیلترشکن روشن) — پیام آماده دارد
            is NetworkConnectivityException -> e.message
            is HttpException                -> extractFromHttpException(e)
            is SocketTimeoutException       -> "زمان اتصال به سرور تمام شد. لطفاً دوباره تلاش کنید."
            is UnknownHostException         -> "ارتباط با سرور برقرار نشد. اتصال اینترنت خود را بررسی کنید."
            is IOException                  -> "خطای ارتباط با سرور. لطفاً دوباره تلاش کنید."
            else                            -> e.localizedMessage
                    ?: "خطای ناشناخته. لطفاً دوباره تلاش کنید."
        }
    }

    private fun extractFromHttpException(e: HttpException): String {
        val code = e.code()
        val raw = try {
            e.response()
                ?.errorBody()
                ?.string()
                .orEmpty()
        } catch (_: Exception) {
            ""
        }

        // تلاش برای خواندن message از پاکت استاندارد
        val parsed = parseMessage(raw)
        if (!parsed.isNullOrBlank()) return parsed

        // پیام‌های پیش‌فرض بر اساس کد
        return when (code) {
            400 -> "درخواست نامعتبر است."
            401 -> "نام کاربری یا رمز عبور اشتباه است."
            403 -> "شما دسترسی لازم برای این عملیات را ندارید."
            404 -> "موردی یافت نشد."
            422 -> "اطلاعات ارسالی معتبر نیست."
            429 -> "تعداد درخواست‌ها زیاد است. کمی صبر کنید."
            in 500..599 -> "خطای سرور. لطفاً بعداً تلاش کنید."
            else -> "خطای ناشناخته (کد $code)."
        }
    }

    /** تلاش برای پیدا کردن message در پاسخ JSON */
    private fun parseMessage(raw: String): String? {
        if (raw.isBlank()) return null
        return try {
            // ابتدا با JSONObject (سریع‌تر)
            val obj = JSONObject(raw)
            val msg = obj.optString(
                "message",
                null
            )
            if (!msg.isNullOrBlank()) {
                // اگر خودش آبجکت validation errors باشد، آن را رد می‌کنیم تا پیام پیش‌فرض نمایش داده شود
                msg
            } else null
        } catch (_: Exception) {
            try {
                // fallback: Gson
                val map = gson.fromJson(
                    raw,
                    Map::class.java
                ) as? Map<*, *>
                map?.get("message") as? String
            } catch (_: Exception) {
                null
            }
        }
    }
}
