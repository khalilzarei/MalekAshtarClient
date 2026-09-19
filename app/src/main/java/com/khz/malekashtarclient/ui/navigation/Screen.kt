package com.khz.malekashtarclient.ui.navigation

/**
 * مسیرهای صفحات
 *
 * الگو: sealed class با companion object برای ساخت مسیرهای پویا
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object ChangePassword : Screen("change_password")
    data object Dashboard : Screen("dashboard")

    // صفحات اصلی
    data object NewsList : Screen("news_list")
    data object Classes : Screen("classes")
    data object Finance : Screen("finance")
    data object Matches : Screen("matches")
    data object Profile : Screen("profile")

    // چت
    data object ChatRoomList : Screen("chat_rooms")
    data object ChatContacts : Screen("chat_contacts")

    /** مسیر Chat با roomId و targetUserId اختیاری
     *
     *  - اگر از ChatRoomListScreen بیاید: roomId پر، userId=null
     *  - اگر از ContactsScreen بیاید: userId پر، roomId=null
     */
    data object Chat : Screen("chat?roomId={roomId}&userId={userId}&title={title}") {
        fun create(
            roomId: Int? = null,
            targetUserId: Int? = null,
            title: String? = null
        ): String {
            val sb = StringBuilder("chat?")
            sb.append("roomId=").append(roomId ?: -1)
            sb.append("&userId=").append(targetUserId ?: -1)
            sb.append("&title=").append(title ?: "")
            return sb.toString()
        }
    }

    companion object {
        /** ساخت مسیر Chat از roomId موجود در لیست */
        fun chatWithRoom(roomId: Int, title: String?): String =
            Chat.create(roomId = roomId, targetUserId = null, title = title)

        /** ساخت مسیر Chat از مخاطب جدید */
        fun chatWithUser(userId: Int): String =
            Chat.create(roomId = null, targetUserId = userId, title = null)
    }
}
