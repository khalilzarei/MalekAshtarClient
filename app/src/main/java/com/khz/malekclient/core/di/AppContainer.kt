package com.khz.malekclient.core.di

import android.content.Context
import com.google.gson.GsonBuilder
import com.khz.malekclient.core.local.SessionManager
import com.khz.malekclient.core.network.AuthInterceptor
import com.khz.malekclient.core.network.BooleanAdapter
import com.khz.malekclient.core.network.ConnectivityGuardInterceptor
import com.khz.malekclient.core.network.ConnectivityMonitor
import com.khz.malekclient.core.network.SchemeFallbackInterceptor
import com.khz.malekclient.core.util.Constants
import com.khz.malekclient.data.remote.AuthApi
import com.khz.malekclient.data.remote.ChatApi
import com.khz.malekclient.data.remote.ClientApi
import com.khz.malekclient.data.remote.PingApi
import com.khz.malekclient.data.repository.AuthRepository
import com.khz.malekclient.data.repository.ChatRepository
import com.khz.malekclient.data.repository.ClientRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

class AppContainer(context: Context) {

    // ═════════════════════════════════════════════
// Local Storage
// ═════════════════════════════════════════════
    val sessionManager: SessionManager by lazy { SessionManager(context) }

    /** ViewModelFactory (بعداً ساخته می‌شود) */
    val viewModelFactory: ViewModelFactory by lazy { ViewModelFactory(this) }

    // ═════════════════════════════════════════════
// Network Core
// ═════════════════════════════════════════════
    /** مانیتور وضعیت اینترنت (وصل/قطع/فیلترشکن) */
    val connectivity: ConnectivityMonitor by lazy {
        ConnectivityMonitor(context).also { it.start() }
    }

    /**
     * آدرس پینگ روی http — برای چک دسترسی سرور وقتی https (SSL) در دسترس نیست.
     * از BASE_URL استخراج می‌شود:  https://host/api/v1/  →  http://host/api/v1/ping
     */
    private val httpPingUrl: String by lazy {
        val hostPath = Constants.BASE_URL.substringAfter("://")
        "http://" + hostPath.trimEnd('/') + "/ping"
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(sessionManager)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // اول از همه: چک وضعیت اتصال (اینترنت قطع / فیلترشکن) — fail سریع
            .addInterceptor(ConnectivityGuardInterceptor(connectivity))
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            // در انتها: اگر https شکست خورد (سرور بدون SSL) → چک پینگ + تلاش با http
            .addInterceptor(SchemeFallbackInterceptor(httpPingUrl))
            .connectTimeout(
                Constants.CONNECT_TIMEOUT,
                TimeUnit.SECONDS
            )
            .readTimeout(
                Constants.READ_TIMEOUT,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                Constants.WRITE_TIMEOUT,
                TimeUnit.SECONDS
            )
            .build()
    }

    private val retrofit: Retrofit by lazy {
// Gson با BooleanAdapter سفارشی که 1/0/"true"/"false"/null را می‌پذیرد
        val gson = GsonBuilder().registerTypeAdapter(
            Boolean::class.java,
            BooleanAdapter()
        )
            .registerTypeAdapter(
                Boolean::class.javaPrimitiveType,
                BooleanAdapter()
            )
            .create()

        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // ═════════════════════════════════════════════
// API Services (فقط آنچه اپ بازیکن نیاز دارد)
// ═════════════════════════════════════════════
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val clientApi: ClientApi by lazy { retrofit.create(ClientApi::class.java) }
    val chatApi: ChatApi by lazy { retrofit.create(ChatApi::class.java) }
    val pingApi: PingApi by lazy { retrofit.create(PingApi::class.java) }

    // ═════════════════════════════════════════════
// Repositories
// ═════════════════════════════════════════════
    val authRepository: AuthRepository by lazy {
        AuthRepository(
            authApi,
            sessionManager
        )
    }

    val clientRepository: ClientRepository by lazy {
        ClientRepository(clientApi)
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(chatApi)
    }
}
