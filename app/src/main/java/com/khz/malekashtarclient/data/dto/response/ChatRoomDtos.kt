package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

data class ChatRoomDto(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("is_group")
    val isGroup: Boolean? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("image")
    val image: String? = null,

    @SerializedName("users")
    val users: List<ChatRoomUserDto> = emptyList(),

    @SerializedName("last_message")
    val lastMessage: ChatMessageDto? = null,

    @SerializedName("unread_count")
    val unreadCount: Int? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("is_locked")
    val isLocked: Any? = null,

    /**
     * فقط metadata قدیمی/اختیاری.
     * شناسه کاربر چت نیست.
     */
    @SerializedName("player_id")
    val playerId: Int? = null,

    @SerializedName("class_id")
    val classId: Int? = null,

    @SerializedName("subject")
    val subject: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class ChatRoomUserDto(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("full_name")
    val fullName: String? = null,

    @SerializedName("avatar")
    val avatar: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("member_role")
    val memberRole: String? = null
)

data class ChatRoomMemberDto(

    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("full_name")
    val fullName: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("member_role")
    val memberRole: String? = null,

    @SerializedName("last_read_message_id")
    val lastReadMessageId: Int? = null
)

data class ChatRoomListWrapperDto(
    @SerializedName("rooms")
    val rooms: List<ChatRoomDto> = emptyList()
)

data class CreateRoomWrapperDto(
    @SerializedName("room")
    val room: ChatRoomDto? = null
)

data class MessagesWrapperDto(
    @SerializedName("messages")
    val messages: List<ChatMessageDto> = emptyList()
)

data class SendMessageWrapperDto(
    @SerializedName("message")
    val message: ChatMessageDto? = null
)


/* ═══════════════ Wrapper DTOs ═══════════════ */

/** پاکت me/children: data.children = [...] */
data class MyChildrenWrapperDto(
    val children: List<MyChildDto> = emptyList()
)

/** پاکت me/schedule: data.sessions = [...] */
data class MyScheduleWrapperDto(
    val sessions: List<MyScheduleDto> = emptyList()
)

/** پاکت me/news: data.news = [...] */
data class NewsWrapperDto(
    val news: List<NewsDto> = emptyList()
)

/** پاکت me/classes: data.classes = [...] */
data class MyClassesWrapperDto(
    val classes: List<MyClassDto> = emptyList()
)

/** پاکت me/finance: data.finance = [...] */
data class MyFinanceWrapperDto(
    val finance: List<MyFinanceDto> = emptyList()
)

/** پاکت me/matches: data.matches = [...] */
data class MyMatchesWrapperDto(
    val matches: List<MyMatchDto> = emptyList()
)

