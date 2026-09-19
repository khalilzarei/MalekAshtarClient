package com.khz.malekashtarclient.domain.model

/**
 * مخاطب قابل گفتگو (از me/chat-contacts)
 */
data class ChatContact(
    val userId: Int,
    val fullName: String,
    val role: String,                   // admin | coach
    val avatarUrl: String?,
    val classTitle: String?
)

/**
 * اتاق چت (از chat/rooms یا POST chat/rooms)
 *
 * - roomType: player_admin | coach_admin | player_coach | age_group
 * - برای age_group: targetUserId = null، targetUserName = title of age group
 * - برای ۲ نفره: targetUserId و targetUserName پر
 */
data class ChatRoom(
    val id: Int,
    val roomType: String,
    val targetUserId: Int?,
    val targetUserName: String?,
    val targetUserRole: String?,
    val targetUserAvatar: String?,
    val playerId: Int?,
    val classId: Int?,
    val classTitle: String?,
    val ageGroupId: Int?,
    val ageGroupTitle: String?,
    val isLocked: Boolean,
    val memberCount: Int,
    val subject: String?,
    val lastMessage: String?,
    val lastMessageAt: String?,
    val unreadCount: Int,
    val members: List<ChatRoomMember>
) {
    val isGroup: Boolean get() = roomType == "age_group"
    val isPrivate: Boolean get() = !isGroup
}

data class ChatRoomMember(
    val userId: Int,
    val fullName: String,
    val role: String?,
    val avatarUrl: String?
)

/**
 * پیام چت
 */
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
    val createdAt: String?
)
