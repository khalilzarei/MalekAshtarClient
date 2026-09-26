package com.khz.malekclient.domain.model

data class ChatContact(
    val userId: Int,
    val fullName: String,
    val role: String,
    val avatarUrl: String?,
    val classTitle: String?
)

data class ChatRoom(
    val id: Int,
    val isGroup: Boolean,
    val title: String,
    val image: String?,
    val users: List<ChatRoomUser>,
    val unreadCount: Int,
    val lastMessage: ChatMessage?,
    val status: String,
    val isLocked: Boolean,
    val playerId: Int?,
    val classId: Int?,
    val subject: String?,
    val createdAt: String?,
    val updatedAt: String?
) {
    val isPrivate: Boolean
        get() = !isGroup
}

data class ChatRoomUser(
    val id: Int,
    val fullName: String,
    val avatar: String?,
    val role: String?,
    val memberRole: String?,

    /**
     * آخرین پیامی که این عضو خوانده — برای تیک سبز ✓✓:
     * پیام من خوانده شده ⇐ lastReadMessageId >= پیام.id
     */
    val lastReadMessageId: Int = 0,
    val mobile: String? = null
)

data class ChatRoomMember(
    val userId: Int,
    val fullName: String,
    val role: String?,
    val memberRole: String?,
    val lastReadMessageId: Int?
)

data class ChatMessage(
    val id: Int,
    val roomId: Int,
    val senderId: Int?,
    val senderName: String?,
    val senderRole: String?,
    val senderAvatar: String?,
    val messageType: String,
    val body: String,
    val isRead: Boolean,
    val createdAt: String?,

    /**
     * زمان تحویل پیام به اپِ گیرنده (UTC).
     * null = هنوز تحویل نشده (تیک ✓) — اگر مقدار دارد (تیک ✓✓).
     */
    val deliveredAt: String? = null
)