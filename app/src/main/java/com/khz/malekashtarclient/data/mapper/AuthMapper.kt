package com.khz.malekashtarclient.data.mapper

import com.khz.malekashtarclient.data.dto.response.LoginResponseDto
import com.khz.malekashtarclient.data.dto.response.UserDto
import com.khz.malekashtarclient.domain.model.LoginResult
import com.khz.malekashtarclient.domain.model.User

/**
 * تبدیل DTO → Domain Model مربوط به احراز هویت
 *
 * همه‌ی فیلدهای nullable DTO با مقادیر پیش‌فرض ایمن به non-null تبدیل می‌شوند.
 */
object AuthMapper {

    fun UserDto.toDomain(): User = User(
        id = id ?: 0,
        fullName = fullName?.takeIf { it.isNotBlank() } ?: fullNameFromParts(),
        role = role?.takeIf { it.isNotBlank() } ?: "player",
        mustChangePassword = mustChangePassword == true,
        avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
        mobile = mobile?.takeIf { it.isNotBlank() },
        nationalCode = nationalCode?.takeIf { it.isNotBlank() }
    )

    fun LoginResponseDto.toResult(): LoginResult? {
        val t = extractToken() ?: return null
        val u = user?.toDomain() ?: return null
        return LoginResult(token = t, user = u)
    }

    /** ترکیب firstName + lastName اگر fullName موجود نبود */
    private fun UserDto.fullNameFromParts(): String {
        val f = firstName?.takeIf { it.isNotBlank() } ?: ""
        val l = lastName?.takeIf { it.isNotBlank() } ?: ""
        return "$f $l".trim().ifBlank { "بازیکن" }
    }
}
