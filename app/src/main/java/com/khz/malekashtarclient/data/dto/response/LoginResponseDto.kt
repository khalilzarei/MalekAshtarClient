package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * پاکت data در پاسخ POST auth/login
 * شامل توکن + اطلاعات کاربر
 */
data class LoginResponseDto(
    @SerializedName("token") val token: String? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,        // "Bearer"
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("user") val user: UserDto? = null
) {
    /** استخراج توکن (token یا access_token، هر کدام که باشد) */
    fun extractToken(): String? = token?.takeIf { it.isNotBlank() }
            ?: accessToken?.takeIf { it.isNotBlank() }
}
