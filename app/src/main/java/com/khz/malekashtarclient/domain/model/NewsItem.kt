package com.khz.malekashtarclient.domain.model

/**
 * یک خبر (از me/news)
 */
data class NewsItem(
    val id: Int,
    val title: String,
    val body: String?,
    val publishedAt: String?,       // YYYY-MM-DD HH:MM:SS
    val createdAt: String?          // fallback برای نمایش تاریخ
) {
    /** تاریخ نمایشی: published_at یا created_at، هر کدام موجود باشد */
    val displayDate: String?
        get() = publishedAt?.takeIf { it.isNotBlank() } ?: createdAt
}
