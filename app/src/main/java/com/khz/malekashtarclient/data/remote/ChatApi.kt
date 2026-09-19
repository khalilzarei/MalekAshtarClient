package com.khz.malekashtarclient.data.remote

import com.khz.malekashtarclient.core.network.ApiResponse
import com.khz.malekashtarclient.data.dto.request.MarkAsReadRequest
import com.khz.malekashtarclient.data.dto.request.SendMessageRequest
import com.khz.malekashtarclient.data.dto.response.ChatMessageDto
import com.khz.malekashtarclient.data.dto.response.ChatRoomDto
import com.khz.malekashtarclient.data.dto.response.ChatRoomListWrapperDto
import com.khz.malekashtarclient.data.dto.response.CreateRoomWrapperDto
import com.khz.malekashtarclient.data.dto.response.MessagesWrapperDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpointهای چت (chat/*)
 *
 * نکته: room_type را کلاینت نمی‌فرستد — سرور از روی نقش دو کاربر خودش استنتاج می‌کند.
 */
interface ChatApi {

    /** GET chat/rooms → data.rooms[] */
    @GET("chat/rooms")
    suspend fun rooms(): ApiResponse<ChatRoomListWrapperDto>

    /**
     * POST chat/rooms — ساخت/دریافت اتاق دو نفره (idempotent)
     * بدنه: {target_user_id: N} — room_type نفرست
     */
    @POST("chat/rooms")
    suspend fun createOrGetPrivateRoom(
        @Body body: Map<String, Int>
    ): ApiResponse<CreateRoomWrapperDto>

    /** GET chat/rooms/{id}/messages?limit=50 → data.messages[] */
    @GET("chat/rooms/{id}/messages")
    suspend fun messages(
        @Path("id") roomId: Int,
        @Query("limit") limit: Int = 50
    ): ApiResponse<MessagesWrapperDto>

    /** POST chat/rooms/{id}/messages */
    @POST("chat/rooms/{id}/messages")
    suspend fun sendMessage(
        @Path("id") roomId: Int,
        @Body body: SendMessageRequest
    ): ApiResponse<MessageWrapperDto>

    /** POST chat/rooms/{id}/read */
    @POST("chat/rooms/{id}/read")
    suspend fun markAsRead(
        @Path("id") roomId: Int,
        @Body body: MarkAsReadRequest
    ): ApiResponse<Any?>

    /** POST chat/rooms/{id}/lock → فقط ادمین (این اپ دکمه ندارد ولی API موجود است) */
    @POST("chat/rooms/{id}/lock")
    suspend fun lockRoom(@Path("id") roomId: Int): ApiResponse<Any?>

    /** POST chat/rooms/{id}/unlock */
    @POST("chat/rooms/{id}/unlock")
    suspend fun unlockRoom(@Path("id") roomId: Int): ApiResponse<Any?>
}

/** پاکت data.message برای ارسال پیام */
data class MessageWrapperDto(
    val message: ChatMessageDto? = null
)
