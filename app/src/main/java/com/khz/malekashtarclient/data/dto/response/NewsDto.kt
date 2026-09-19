package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * پاسخ GET me/news → data.news[]
 *
 * فقط اخبار منتشرشده‌ی مجاز برای این کاربر (فیلتر سمت سرور)
 */
data class NewsDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("publish_at") val publishAt: String? = null,
    @SerializedName("published_at") val publishedAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
