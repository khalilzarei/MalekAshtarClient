package com.khz.malekclient.data.mapper

import com.khz.malekclient.data.dto.response.LoginResponseDto
import com.khz.malekclient.data.dto.response.UserDto
import com.khz.malekclient.domain.model.LoginResult
import com.khz.malekclient.domain.model.User

/**
 * تبدیل DTO → Domain Model مربوط به احراز هویت
 *
 * همه‌ی فیلدهای nullable DTO با مقادیر پیش‌فرض ایمن به non-null تبدیل می‌شوند.
 */
object AuthMapper {

    fun UserDto.toDomain(): User = User(
        id = id
            ?: 0,
        fullName = fullName?.takeIf { it.isNotBlank() }
                ?: fullNameFromParts(),
        role = role?.takeIf { it.isNotBlank() }
                ?: "player",
        // mustChangePassword می‌تواند true/false/1/0/"1"/"0" باشد
        mustChangePassword = parseFlexibleBoolean(mustChangePassword),
        avatarUrl = resolvedAvatarUrl,
        mobile = mobile?.takeIf { it.isNotBlank() },
        nationalCode = nationalCode?.takeIf { it.isNotBlank() })

    fun LoginResponseDto.toResult(): LoginResult? {
        val t = extractToken()
                ?: return null
        val u = user?.toDomain()
                ?: return null
        return LoginResult(
            token = t,
            user = u
        )
    }

    /** ترکیب firstName + lastName اگر fullName موجود نبود */
    private fun UserDto.fullNameFromParts(): String {
        val f = firstName?.takeIf { it.isNotBlank() }
                ?: ""
        val l = lastName?.takeIf { it.isNotBlank() }
                ?: ""
        return "$f $l".trim()
            .ifBlank { "بازیکن" }
    }

    /**
     * تبدیل انعطاف‌پذیر به Boolean:
     *  - Boolean → همان مقدار
     *  - Int (1/0) → 1=true, 0=false
     *  - Long → مشابه Int
     *  - String ("true"/"false"/"1"/"0"/"yes"/"no") → مقدار بولی
     *  - null → false (پیش‌فرض: نیازی به تغییر رمز نیست)
     */
    private fun parseFlexibleBoolean(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Boolean -> value
            is Int -> value != 0
            is Long -> value != 0L
            is Double -> value != 0.0
            is Float -> value != 0f
            is String -> when (value.trim()
                .lowercase()) {
                "true", "1", "yes", "بله", "on" -> true
                else                            -> false
            }

            else -> false
        }
    }
}
