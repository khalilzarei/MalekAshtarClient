package com.khz.malekashtarclient.data.remote

import com.khz.malekashtarclient.core.network.ApiResponse
import com.khz.malekashtarclient.data.dto.request.ChangePasswordRequest
import com.khz.malekashtarclient.data.dto.request.LoginRequest
import com.khz.malekashtarclient.data.dto.response.LoginResponseDto
import com.khz.malekashtarclient.data.dto.response.UserDto
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
