package com.khz.malekashtarclient.data.dto.request

/**
 * بدنه‌ی POST chat/rooms
 *
 * در این اپ، room_type را کلاینت نمی‌فرستیم (سرور از روی نقش دو کاربر استنتاج می‌کند).
 * پس یک Map ساده {target_user_id: N} می‌فرستیم — همان کاری که ChatApi.createOrGetPrivateRoom انجام می‌دهد.
 */
data class CreatePrivateRoomRequest(
    val target_user_id: Int
)
