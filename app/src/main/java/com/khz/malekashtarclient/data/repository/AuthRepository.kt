package com.khz.malekashtarclient.data.repository

import com.khz.malekashtarclient.core.local.SessionManager
import com.khz.malekashtarclient.core.network.ApiErrorHandler
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.data.dto.request.ChangePasswordRequest
import com.khz.malekashtarclient.data.dto.request.LoginRequest
import com.khz.malekashtarclient.data.mapper.AuthMapper.toDomain
import com.khz.malekashtarclient.data.mapper.AuthMapper.toResult
import com.khz.malekashtarclient.data.remote.AuthApi
import com.khz.malekashtarclient.domain.model.LoginResult
import com.khz.malekashtarclient.domain.model.User

/**
 * ریپازیتوری احراز هویت
 *
 * هر متد:
 *  - با try/catch خطای شبکه/سرور را می‌گیرد
 *  - خروجی NetworkResult<T> برمی‌گرداند
 *  - پیام خطا از r.message یا ApiErrorHandler.extractMessage(e)
 *
 * نکته: متدهای login/changePassword پس از موفقیت، token + userId + role
 * را در SessionManager ذخیره می‌کنند.
 */
class AuthRepository(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) {

    /**
     * ورود
     * در صورت موفقیت، token + userId + role در SessionManager ذخیره می‌شود.
     */
    suspend fun login(identifier: String, password: String): NetworkResult<LoginResult> {
        return try {
            val response = api.login(LoginRequest(identifier, password))
            if (!response.success) {
                return NetworkResult.Error(response.message ?: "خطا در ورود")
            }
            val result = response.data?.toResult()
                ?: return NetworkResult.Error("پاسخ سرور نامعتبر است")

            // ذخیره‌ی session
            sessionManager.saveToken(result.token)
            sessionManager.saveUserInfo(
                userId = result.user.id.toString(),
                role = result.user.role
            )

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(ApiErrorHandler.extractMessage(e))
        }
    }

    /** دریافت اطلاعات کاربر جاری */
    suspend fun me(): NetworkResult<User> {
        return try {
            val response = api.me()
            if (!response.success) {
                return NetworkResult.Error(response.message ?: "خطا در دریافت اطلاعات کاربر")
            }
            val user = response.data?.toDomain()
                ?: return NetworkResult.Error("پاسخ سرور نامعتبر است")
            NetworkResult.Success(user)
        } catch (e: Exception) {
            NetworkResult.Error(ApiErrorHandler.extractMessage(e))
        }
    }

    /** تغییر رمز عبور (پس از موفقیت mustChangePassword=false می‌شود) */
    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ): NetworkResult<User> {
        return try {
            val response = api.changePassword(
                ChangePasswordRequest(oldPassword, newPassword, newPasswordConfirmation)
            )
            if (!response.success) {
                return NetworkResult.Error(response.message ?: "خطا در تغییر رمز عبور")
            }
            // پس از تغییر موفق، اطلاعات کاربر را دوباره بگیر
            me()
        } catch (e: Exception) {
            NetworkResult.Error(ApiErrorHandler.extractMessage(e))
        }
    }

    /** خروج از حساب — توکن و اطلاعات کاربر پاک می‌شود (rememberMe محفوظ) */
    suspend fun logout(): NetworkResult<Unit> {
        return try {
            api.logout()
            sessionManager.clearSession()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            // حتی اگر API fail شود، session را پاک کن
            try { sessionManager.clearSession() } catch (_: Exception) {}
            NetworkResult.Error(ApiErrorHandler.extractMessage(e))
        }
    }
}
