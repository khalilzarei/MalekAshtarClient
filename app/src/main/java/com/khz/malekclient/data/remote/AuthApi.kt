package com.khz.malekclient.data.remote

import com.khz.malekclient.core.network.ApiResponse
import com.khz.malekclient.data.dto.request.ChangePasswordRequest
import com.khz.malekclient.data.dto.request.LoginRequest
import com.khz.malekclient.data.dto.response.LoginResponseDto
import com.khz.malekclient.data.dto.response.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Endpointهای احراز هویت
 *
 * POST auth/login     → ورود (data: {token, user})
 * POST auth/logout    → خروج
 * POST auth/change-password → تغییر رمز
 * GET  auth/me        → اطلاعات کاربر جاری
 */
interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponseDto>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Any?>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiResponse<Any?>

    @GET("auth/me")
    suspend fun me(): ApiResponse<UserDto>
}
