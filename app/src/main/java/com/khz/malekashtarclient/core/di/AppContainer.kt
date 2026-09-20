package com.khz.malekashtarclient.core.di

import android.content.Context
import com.google.gson.GsonBuilder
import com.khz.malekashtarclient.core.local.SessionManager
import com.khz.malekashtarclient.core.network.AuthInterceptor
import com.khz.malekashtarclient.core.network.BooleanAdapter
import com.khz.malekashtarclient.core.util.Constants
import com.khz.malekashtarclient.data.remote.AuthApi
import com.khz.malekashtarclient.data.remote.ChatApi
import com.khz.malekashtarclient.data.remote.ClientApi
import com.khz.malekashtarclient.data.repository.AuthRepository
import com.khz.malekashtarclient.data.repository.ChatRepository
import com.khz.malekashtarclient.data.repository.ClientRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

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
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
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
