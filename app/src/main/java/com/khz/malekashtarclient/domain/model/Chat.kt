package com.khz.malekashtarclient.domain.model

/**
 * مخاطب قابل گفتگو (از me/chat-contacts)
 *
 * ساختار مطابق ClientService::chatContacts در سرور.
 */
data class ChatContact(
    val userId: Int,
    val fullName: String,
    val role: String,                   // admin | coach
    val avatarUrl: String?,             // همیشه پر (AvatarService با fallback)
    val classTitle: String?
)

/**
 * اتاق چت — دقیقاً مطابق ChatService::hydrateRoom در سرور
 *
 * فیلدهای سرور:
 *  - id, room_type, status, subject, player_id, class_id, age_group_id
 *  - created_at, updated_at
 *  - target_user_{id,name,role} (برای age_group همگی null)
 *  - age_group_title (فقط age_group)
 *  - is_locked, member_count, unread_count
 *  - members (آرایه‌ی ChatRoomMember بدون avatar_url)
 *  - last_message (شیء ChatMessage یا null)
 *
 * فیلدهای کمکی client (از last_message استخراج می‌شوند):
 *  - lastMessageBody: متن پیام
 *  - lastMessageAt: زمان پیام
 *  - lastMessageSenderName: نام فرستنده
 *  - lastMessageSenderId: id فرستنده
 *
 * فیلدهای آینده‌نگر (فعلاً null):
 *  - targetUserAvatar: سرور نمی‌فرستد
 *  - classTitle: سرور نمی‌فرستد (فقط در contacts موجود است)
 */
data class ChatRoom(
    // ── فیلدهای اصلی سرور ──
    val id: Int,
    val roomType: String,                                          // player_admin | player_coach | coach_admin | age_group
    val status: String,                                            // active
    val targetUserId: Int?,
    val targetUserName: String?,
    val targetUserRole: String?,
    val ageGroupId: Int?,
    val ageGroupTitle: String?,
    val isLocked: Boolean,
    val memberCount: Int,
    val unreadCount: Int,
    val members: List<ChatRoomMember>,

    // ── اختیاری/nullable ──
    val playerId: Int?,
    val classId: Int?,
    val subject: String?,
    val createdAt: String?,
    val updatedAt: String?,

    // ── فیلدهای کمکی (extracted از last_message) ──
    val lastMessageBody: String?,
    val lastMessageAt: String?,                                    // sent_at یا created_at
    val lastMessageSenderName: String?,
    val lastMessageSenderId: Int?,

    // ── آینده‌نگر (فعلاً null) ──
    val targetUserAvatar: String?,
    val classTitle: String?
) {
    val isGroup: Boolean get() = roomType == "age_group"
    val isPrivate: Boolean get() = !isGroup
}

/**
 * عضو اتاق — مطابق hydrateRoom
 *
 * نکته: avatar_url در hydrateRoom فرستاده نمی‌شود.
 * اگر نیاز به آواتار دارید، از contacts استفاده کنید.
 */
data class ChatRoomMember(
    val userId: Int,
    val fullName: String,
    val role: String?,                                             // user_role از join
    val memberRole: String?,                                       // owner | member
    val lastReadMessageId: Int?
)

/**
 * پیام چت
 *
 * senderAvatar در پاسخ سرور وجود ندارد (sender شامل avatar_url نیست).
 * آواتار فرستنده در ChatScreen از روی senderId از طریق contacts قابل نمایش است.
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
