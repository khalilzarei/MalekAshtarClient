package com.khz.malekclient.core.util

/**
 * ثابت‌های سراسری اپ بازیکن/والدین
 */
object Constants {

    const val BASE_URL = "https://api.malekashtarkaz.ir/api/v1/"
    
    const val STORAGE_URL = "https://api.malekashtarkaz.ir/"

    /** Timeouts (بر حسب ثانیه) */
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 60L
    const val WRITE_TIMEOUT = 30L

    /** نقش‌های کاربری */
    const val ROLE_PLAYER = "player"
    const val ROLE_COACH = "coach"
    const val ROLE_ADMIN = "admin"

    /** نوع پیام چت */
    const val MESSAGE_TYPE_TEXT = "text"

    /** نوع اتاق چت */
    const val ROOM_TYPE_PLAYER_ADMIN = "player_admin"
    const val ROOM_TYPE_COACH_ADMIN = "coach_admin"
    const val ROOM_TYPE_PLAYER_COACH = "player_coach"
    const val ROOM_TYPE_AGE_GROUP = "age_group"
}
