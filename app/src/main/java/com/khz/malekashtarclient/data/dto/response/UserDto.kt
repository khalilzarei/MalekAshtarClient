package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * اطلاعات کاربر (پاسخ auth/me یا داخل login response)
 *
 * همه‌ی فیلدها nullable چون ممکن است غایب باشند (طبق بند ۹ پرامپت)
 */
data class UserDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("national_code") val nationalCode: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("must_change_password") val mustChangePassword: Boolean? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("birth_date") val birthDate: String? = null
)
