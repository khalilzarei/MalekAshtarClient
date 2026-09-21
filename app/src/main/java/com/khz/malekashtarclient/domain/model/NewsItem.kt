package com.khz.malekashtarclient.domain.model

/**
 * یک خبر (از me/news)
 *
 * `media` شامل عکس‌ها و فیلم‌های متصل به خبر است. اگر خبری رسانه نداشته
 * باشد، لیست خالی می‌ماند و اسلایدر فقط متن خبر را نشان می‌دهد.
 */
data class NewsItem(
    val id: Int,
    val title: String,
    val body: String?,
    val publishedAt: String?,       // YYYY-MM-DD HH:MM:SS
    val createdAt: String?,         // fallback برای نمایش تاریخ
    val media: List<NewsMedia> = emptyList()
) {
    /** تاریخ نمایشی: published_at یا created_at، هر کدام موجود باشد */
    val displayDate: String?
        get() = publishedAt?.takeIf { it.isNotBlank() } ?: createdAt

    val images: List<NewsMedia> get() = media.filter { !it.isVideo }

    val videos: List<NewsMedia> get() = media.filter { it.isVideo }

    val hasMedia: Boolean get() = media.isNotEmpty()

    /** رسانه‌ای که به‌عنوان کاور اسلایدر استفاده می‌شود: اول عکس، بعد فیلم */
    val coverMedia: NewsMedia? get() = images.firstOrNull() ?: videos.firstOrNull()

    /** خلاصه‌ی تعداد، مثل «۲ عکس · ۱ فیلم» */
    val mediaSummary: String?
        get() {
            if (media.isEmpty()) return null

            return listOfNotNull(
                if (images.isNotEmpty()) "${images.size} عکس" else null,
                if (videos.isNotEmpty()) "${videos.size} فیلم" else null
            ).joinToString(" · ")
        }
}
