package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * اطلاعات کاربر (پاسخ auth/me یا داخل login response)
 *
 * همه‌ی فیلدها nullable چون ممکن است غایب باشند (طبق بند ۹ پرامپت)
 *
 * نکته: mustChangePassword از Any? استفاده می‌کند چون سرور ممکن است:
 *  - true/false (بولی)
 *  - 1/0 (عدد)
 *  - "true"/"false"/"1"/"0" (رشته)
 * بفرستد. در mapper به Boolean تبدیل می‌شود.
 */
data class UserDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("national_code") val nationalCode: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null,                  // player | admin | coach
    @SerializedName("status") val status: String? = null,              // active | inactive
    @SerializedName("must_change_password") val mustChangePassword: Any? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("avatar_path") val avatarPath: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("birth_date") val birthDate: String? = null,
    @SerializedName("last_login_at") val lastLoginAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
) {
    /** استخراج URL آواتار از avatar_url یا avatar_path (سرور شما avatar_path می‌فرستد) */
    val resolvedAvatarUrl: String?
        get() = avatarUrl?.takeIf { it.isNotBlank() }
                ?: avatarPath?.takeIf { it.isNotBlank() }
}
