package com.khz.malekashtarclient.data.dto.request

import com.google.gson.annotations.SerializedName

/**
 * بدنه‌ی POST auth/change-password
 *
 * سرور بررسی می‌کند:
 *  - new_password ≥ 8 کاراکتر
 *  - new_password ≠ old_password
 *  - new_password == new_password_confirmation
 */
data class ChangePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("new_password_confirmation") val newPasswordConfirmation: String
)
