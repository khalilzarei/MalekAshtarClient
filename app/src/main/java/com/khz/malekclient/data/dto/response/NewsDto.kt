package com.khz.malekclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * پاسخ GET me/news → data.news[]
 *
 * فقط اخبار منتشرشده‌ی مجاز برای این کاربر (فیلتر سمت سرور)
 *
 * `media` عکس‌ها و فیلم‌های متصل به خبر است. سرور از رابطه‌ی
 * related_type = "news" جدول football_media پر می‌کند.
 */
data class NewsDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("publish_at") val publishAt: String? = null,
    @SerializedName("published_at") val publishedAt: String? = null,
    @SerializedName("media") val media: List<MediaDto>? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
