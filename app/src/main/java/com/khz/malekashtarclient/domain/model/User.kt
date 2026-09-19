package com.khz.malekashtarclient.domain.model

/**
 * اطلاعات حساب کاربری (auth/me یا داخل login response)
 *
 * نقش در این اپ همیشه "player" است.
 */
data class User(
    val id: Int,
    val fullName: String,
    val role: String,
    val mustChangePassword: Boolean,
    val avatarUrl: String? = null,
    val mobile: String? = null,
    val nationalCode: String? = null
)

/** نتیجه‌ی فراخوانی login */
data class LoginResult(
    val token: String,
    val user: User
)
