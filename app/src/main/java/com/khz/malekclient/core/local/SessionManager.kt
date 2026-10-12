package com.khz.malekclient.core.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * مدیریت اطلاعات جلسه‌ی کاربر (token, userId, role, rememberMe)
 * با استفاده از DataStore Preferences
 */
class SessionManager(private val context: Context) {

    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
    private val DASHBOARD_CACHE_KEY = stringPreferencesKey("dashboard_cache_json")

    /** کش حافظه‌ای توکن — از runBlocking تکراری روی DataStore جلوگیری می‌کند */
    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var cachedTokenLoaded = false

    val authToken: Flow<String?> = context.dataStore.data.map { preferences -> preferences[TOKEN_KEY] }

    val userId: Flow<String?> = context.dataStore.data.map { preferences -> preferences[USER_ID_KEY] }

    val userRole: Flow<String?> = context.dataStore.data.map { preferences -> preferences[USER_ROLE_KEY] }

    val rememberMe: Flow<Boolean> = context.dataStore.data.map { preferences ->
            preferences[REMEMBER_ME_KEY]
                    ?: false
        }

    suspend fun saveToken(token: String) {
        cachedToken = token
        cachedTokenLoaded = true
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserInfo(
        userId: String,
        role: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_ROLE_KEY] = role
        }
    }

    suspend fun setRememberMe(remember: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMEMBER_ME_KEY] = remember
        }
    }

    /** فقط توکن و اطلاعات کاربر را پاک می‌کند؛ rememberMe محفوظ است (مگر اینکه کاربر نخواسته) */
    suspend fun clearSession() {
        cachedToken = null
        cachedTokenLoaded = true
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_ROLE_KEY)
            val remember = preferences[REMEMBER_ME_KEY]
                    ?: false
            if (!remember) {
                preferences.remove(REMEMBER_ME_KEY)
            }
        }
    }

    /** پاک‌سازی کامل (برای logout واقعی) */
    suspend fun clearAll() {
        cachedToken = null
        cachedTokenLoaded = true
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /** خواندن sync توکن فعلی (برای AuthInterceptor) — کش حافظه‌ای اولویت دارد */
    suspend fun getTokenSync(): String? {
        if (cachedTokenLoaded) return cachedToken
        val token = authToken.first()
        cachedToken = token
        cachedTokenLoaded = true
        return token
    }

    /** خواندن سریع بدون suspend — برای Interceptor وقتی کش آماده است */
    fun peekToken(): String? = if (cachedTokenLoaded) cachedToken else null

    // ── Dashboard cache (آفلاین) ──
    suspend fun saveDashboardCache(json: String) {
        context.dataStore.edit { it[DASHBOARD_CACHE_KEY] = json }
    }

    suspend fun getDashboardCache(): String? {
        return context.dataStore.data.map { it[DASHBOARD_CACHE_KEY] }
            .first()
    }

    val dashboardCacheFlow: Flow<String?> = context.dataStore.data.map { it[DASHBOARD_CACHE_KEY] }
}
