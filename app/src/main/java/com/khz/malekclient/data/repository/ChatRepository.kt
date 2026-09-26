package com.khz.malekclient.data.repository

import com.khz.malekclient.core.network.ApiErrorHandler
import com.khz.malekclient.core.network.NetworkResult
import com.khz.malekclient.core.util.Constants
import com.khz.malekclient.data.dto.request.MarkAsReadRequest
import com.khz.malekclient.data.dto.request.SendMessageRequest
import com.khz.malekclient.data.mapper.ClientMapper.toDomain
import com.khz.malekclient.data.remote.ChatApi
import com.khz.malekclient.domain.model.ChatMessage
import com.khz.malekclient.domain.model.ChatRoom

class ChatRepository(
    private val api: ChatApi
) {

    /**
     * دریافت لیست اتاق‌های گفتگو
     */
    suspend fun rooms(): NetworkResult<List<ChatRoom>> {
        return try {
            val response = api.rooms()

            if (response.success) {
                NetworkResult.Success(response.data?.rooms?.map { it.toDomain() }
                        ?: emptyList())
            } else {
                NetworkResult.Error(
                    message = response.message
                            ?: "دریافت اتاق‌های گفتگو ناموفق بود."
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /**
     * دریافت اتاق خصوصی موجود
     * یا ایجاد آن در صورت عدم وجود
     */
    suspend fun getOrCreatePrivateRoom(
        targetUserId: Int
    ): NetworkResult<ChatRoom> {
        return try {

            val response = api.createOrGetPrivateRoom(
                body = mapOf(
                    "target_user_id" to targetUserId
                )
            )

            if (response.success) {

                val room = response.data?.room

                if (room != null) {
                    NetworkResult.Success(
                        room.toDomain()
                    )
                } else {
                    NetworkResult.Error(
                        message = "اطلاعات اتاق گفتگو دریافت نشد."
                    )
                }

            } else {
                NetworkResult.Error(
                    message = response.message
                            ?: "ایجاد اتاق گفتگو ناموفق بود."
                )
            }

        } catch (e: Exception) {
            NetworkResult.Error(
                message = ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /**
     * دریافت پیام‌های یک اتاق
     */
    suspend fun getMessages(
        roomId: Int,
        limit: Int = 50
    ): NetworkResult<List<ChatMessage>> {
        return try {

            val response = api.messages(
                roomId = roomId,
                limit = limit
            )

            if (response.success) {

                NetworkResult.Success(response.data?.messages?.map { it.toDomain() }
                        ?: emptyList())

            } else {
                NetworkResult.Error(
                    message = response.message
                            ?: "دریافت پیام‌ها ناموفق بود."
                )
            }

        } catch (e: Exception) {
            NetworkResult.Error(
                message = ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /**
     * ارسال پیام متنی
     */
    suspend fun sendMessage(
        roomId: Int,
        body: String
    ): NetworkResult<ChatMessage> {
        return try {

            val request = SendMessageRequest(
                messageType = Constants.MESSAGE_TYPE_TEXT,
                body = body
            )

            val response = api.sendMessage(
                roomId = roomId,
                body = request
            )

            if (response.success) {

                val message = response.data?.message

                if (message != null) {
                    NetworkResult.Success(
                        message.toDomain()
                    )
                } else {
                    NetworkResult.Error(
                        message = "اطلاعات پیام ارسال‌شده دریافت نشد."
                    )
                }

            } else {
                NetworkResult.Error(
                    message = response.message
                            ?: "ارسال پیام ناموفق بود."
                )
            }

        } catch (e: Exception) {
            NetworkResult.Error(
                message = ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /**
     * علامت‌گذاری پیام‌ها به عنوان خوانده‌شده
     */
    suspend fun markAsRead(
        roomId: Int,
        lastReadMessageId: Int
    ): NetworkResult<Unit> {
        return try {

            val response = api.markAsRead(
                roomId = roomId,
                body = MarkAsReadRequest(
                    lastReadMessageId = lastReadMessageId
                )
            )

            if (response.success) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(
                    message = response.message
                            ?: "علامت‌گذاری پیام به عنوان خوانده‌شده ناموفق بود."
                )
            }

        } catch (e: Exception) {
            NetworkResult.Error(
                message = ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /**
     * قفل کردن اتاق گفتگو
     */
    suspend fun lockRoom(
        roomId: Int
    ): NetworkResult<Unit> {
        return try {

            val response = api.lockRoom(roomId)

            if (response.success) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(
                    message = response.message
                            ?: "قفل کردن اتاق گفتگو ناموفق بود."
                )
            }

        } catch (e: Exception) {
            NetworkResult.Error(
                message = ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /**
     * باز کردن قفل اتاق گفتگو
     */
    suspend fun unlockRoom(
        roomId: Int
    ): NetworkResult<Unit> {
        return try {

            val response = api.unlockRoom(roomId)

            if (response.success) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(
                    message = response.message
                            ?: "باز کردن قفل اتاق گفتگو ناموفق بود."
                )
            }

        } catch (e: Exception) {
            NetworkResult.Error(
                message = ApiErrorHandler.extractMessage(e)
            )
        }
    }
}