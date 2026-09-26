package com.khz.malekclient.data.remote

import com.khz.malekclient.core.network.ApiResponse
import com.khz.malekclient.data.dto.request.MarkAsReadRequest
import com.khz.malekclient.data.dto.request.SendMessageRequest
import com.khz.malekclient.data.dto.response.ChatMessageDto
import com.khz.malekclient.data.dto.response.ChatRoomListWrapperDto
import com.khz.malekclient.data.dto.response.CreateRoomWrapperDto
import com.khz.malekclient.data.dto.response.MessagesWrapperDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {

    /**
     * لیست اتاق‌های کاربر فعلی
     */
    @GET("chat/rooms")
    suspend fun rooms(): ApiResponse<ChatRoomListWrapperDto>

    /**
     * ایجاد یا دریافت Private Chat
     *
     * Backend کاربر فعلی را از Token تشخیص می‌دهد.
     *
     * Body:
     * {
     *     "target_user_id": 25
     * }
     */
    @POST("chat/rooms")
    suspend fun createOrGetPrivateRoom(
        @Body body: Map<String, Int>
    ): ApiResponse<CreateRoomWrapperDto>

    /**
     * پیام‌های یک اتاق
     */
    @GET("chat/rooms/{id}/messages")
    suspend fun messages(
        @Path("id") roomId: Int,
        @Query("limit") limit: Int = 50
    ): ApiResponse<MessagesWrapperDto>

    /**
     * ارسال پیام
     */
    @POST("chat/rooms/{id}/messages")
    suspend fun sendMessage(
        @Path("id") roomId: Int,
        @Body body: SendMessageRequest
    ): ApiResponse<MessageWrapperDto>

    /**
     * علامت‌گذاری پیام‌ها به عنوان خوانده‌شده
     */
    @POST("chat/rooms/{id}/read")
    suspend fun markAsRead(
        @Path("id") roomId: Int,
        @Body body: MarkAsReadRequest
    ): ApiResponse<Any?>

    /**
     * قفل اتاق
     */
    @POST("chat/rooms/{id}/lock")
    suspend fun lockRoom(
        @Path("id") roomId: Int
    ): ApiResponse<Any?>

    /**
     * باز کردن قفل اتاق
     */
    @POST("chat/rooms/{id}/unlock")
    suspend fun unlockRoom(
        @Path("id") roomId: Int
    ): ApiResponse<Any?>
}

data class MessageWrapperDto(
    val message: ChatMessageDto? = null
)
