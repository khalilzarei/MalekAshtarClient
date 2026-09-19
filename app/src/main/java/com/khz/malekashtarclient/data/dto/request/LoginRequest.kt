package com.khz.malekashtarclient.data.dto.request

import com.google.gson.annotations.SerializedName

/**
 * بدنه‌ی POST auth/login
 *
 * در این اپ همیشه:
 *   identifier = کد ملی بازیکن
 *   password   = رمز بازیکن
 */
data class LoginRequest(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("password") val password: String
)
