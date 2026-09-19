package com.khz.malekashtarclient.data.repository

import com.khz.malekashtarclient.core.network.ApiErrorHandler
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.core.util.Constants
import com.khz.malekashtarclient.data.dto.request.MarkAsReadRequest
import com.khz.malekashtarclient.data.dto.request.SendMessageRequest
import com.khz.malekashtarclient.data.mapper.ClientMapper.toDomain
import com.khz.malekashtarclient.data.remote.ChatApi
import com.khz.malekashtarclient.domain.model.ChatMessage
import com.khz.malekashtarclient.domain.model.ChatRoom

/**
 * ریپازیتوری چت
 *
 * نکته‌ها:
 *  - room_type را کلاینت نمی‌فرستد — سرور از روی نقش دو کاربر استنتاج می‌کند
 *  - همه‌ی متدها در try/catch با خروجی NetworkResult
 *  - متد sendMessage همیشه flag sending را در finally آزاد می‌کند (در سطح UI)
 */
class ChatRepository(
    private val api: ChatApi
) {

    /** لیست اتاق‌های بازیکن (دو نفره + گروهی) */
    suspend fun rooms(): NetworkResult<List<ChatRoom>> {
        return try {
            val response = api.rooms()
            if (!response.success) {
                NetworkResult.Error(response.message ?: "خطا در دریافت گفتگوها")
            } else {
                val list = response.data?.rooms?.mapNotNull { it.toDomain() }
                    ?: emptyList()
                NetworkResult.Success(list)
            }
        } catch (e: Exception) {
            NetworkResult.Error(ApiErrorHandler.extractMessage(e))
        }
    }

    /**
     * ساخت/دریافت اتاق دو نفره با یک کاربر (idempotent)
     *
     * سرور از روی نقش دو کاربر خودش room_type را تعیین می‌کند.
     *
     * @param targetUserId شناسه‌ی کاربر مقصد
     * @return ChatRoom پر شده (با target_user_name و ...)
     */
    suspend fun getOrCreatePrivateRoom(targetUserId: Int): NetworkResult<ChatRoom> {
        return try {
            val response = api.createOrGetPrivateRoom(mapOf("target_user_id" to targetUserId))
            if (!response.success) {
                NetworkResult.Error(response.message ?: "خطا در ایجاد گفتگو")
            } else {
                val room = response.data?.room?.toDomain()
                    ?: return NetworkResult.Error("پاسخ سرور نامعتبر است")
                NetworkResult.Success(room)
            }
        } catch (e: Exception) {
            NetworkResult.Error(ApiErrorHandler.extractMessage(e))
        }
    }

    /** دریافت پیام‌های یک اتاق (پیش‌فرض: ۵۰ پیام آخر) */
    suspend fun getMessages(roomId: Int, limit: Int = 50): NetworkResult<List<ChatMessage>> {
        return try {
            val response = api.messages(roomId, limit)
            if (!response.success) {
                NetworkResult.Error(response.message ?: "خطا در دریافت پیام‌ها")
            } else {
                val list = response.data?.messages?.mapNotNull { it.toDomain() }
                    ?: emptyList()
                NetworkResult.Success(list)
            }
        } catch (e: Exception) {
            NetworkResult.Error(ApiErrorHandler.extractMessage(e))
        }
    }

    /** ارسال پیام متنی */
    suspend fun sendMessage(roomId: Int, body: String): NetworkResult<ChatMessage> {
        return try {
            if (body.isBlank()) {
                return NetworkResult.Error("پیام نمی‌تواند خالی باشد")
            }
            val response = api.sendMessage(
                roomId,
                SendMessageRequest(messageType = Constants.MESSAGE_TYPE_TEXT, body = body.trim())
            )
            if (!response.success) {
                NetworkResult.Error(response.message ?: "خطا در ارسال پیام")
            } else {
                val msg = response.data?.message?.toDomain()
                    ?: return NetworkResult.Error("پاسخ سرور نامعتبر است")
                NetworkResult.Success(msg)
            }
        } catch (e: Exception) {
            NetworkResult.Error(ApiErrorHandler.extractMessage(e))
        }
    }

    /** ثبت خوانده‌شدن پیام‌ها تا یک پیام مشخص */
    suspend fun markAsRead(roomId: Int, lastReadMessageId: Int): NetworkResult<Unit> {
        return try {
            val response = api.markAsRead(roomId, MarkAsReadRequest(lastReadMessageId))
            if (!response.success) {
                NetworkResult.Error(response.message ?: "خطا در ثبت خواندن")
            } else {
                NetworkResult.Success(Unit)
            }
        } catch (e: Exception) {
            NetworkResult.Error(ApiErrorHandler.extractMessage(e))
        }
    }
}
