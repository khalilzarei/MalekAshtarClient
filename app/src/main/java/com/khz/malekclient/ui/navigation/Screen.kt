package com.khz.malekclient.ui.navigation

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
    data object NewsDetail : Screen("news_detail/{newsId}") {
        fun create(newsId: Int): String = "news_detail/$newsId"
    }

    data object Classes : Screen("classes")
    data object Finance : Screen("finance")
    data object Matches : Screen("matches")
    data object Profile : Screen("profile")

    // چت
    data object ChatRoomList : Screen("chat_rooms")
    data object ChatContacts : Screen("chat_contacts")

    /**
     * مسیر Chat
     *
     * roomId:  برای ورود از لیست اتاق‌ها
     * userId:  برای شروع چت جدید با یک کاربر
     * title:   فعلاً در مسیر نگه داشته شده برای سازگاری
     */
    data object Chat : Screen(
        "chat?roomId={roomId}&userId={userId}&title={title}"
    ) {

        fun create(
            roomId: Int? = null,
            targetUserId: Int? = null,
            title: String? = null
        ): String {
            val sb = StringBuilder("chat?")

            sb.append("roomId=")
                .append(
                    roomId
                            ?: -1
                )

            sb.append("&userId=")
                .append(
                    targetUserId
                            ?: -1
                )

            sb.append("&title=")
                .append(
                    title
                            ?: ""
                )

            return sb.toString()
        }
    }

    companion object {

        /**
         * ورود به Chat از یک Room موجود
         */
        fun chatWithRoom(roomId: Int): String = Chat.create(
            roomId = roomId,
            targetUserId = null,
            title = null
        )

        /**
         * شروع Chat جدید با یک User
         */
        fun chatWithUser(userId: Int): String = Chat.create(
            roomId = null,
            targetUserId = userId,
            title = null
        )
    }
}