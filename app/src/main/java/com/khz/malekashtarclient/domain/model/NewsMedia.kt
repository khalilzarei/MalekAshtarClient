package com.khz.malekashtarclient.domain.model

/**
 * یک رسانه (عکس یا فیلم) متصل به خبر.
 *
 * در اپ کلاینت استفاده‌های اصلی:
 *  - نمایش در اسلایدر بالای داشبورد
 *  - دانلود فایل (عکس یا فیلم)
 *  - پخش درون‌خطی ویدیو
 */
data class NewsMedia(
    val id: Int,
    val fileName: String?,
    val originalName: String?,
    val isVideo: Boolean,
    val mimeType: String,
    val sizeBytes: Long,
    val durationSeconds: Int?,

    /** آدرس دانلود (سرور هدر Content-Disposition: attachment می‌فرستد) */
    val downloadUrl: String?,

    /** آدرس پخش درون‌خطی (بدون اجبار به دانلود + پشتیبانی Range) */
    val streamUrl: String?,

    val thumbnailUrl: String?,
    val description: String?
) {
    val displayName: String get() = originalName ?: fileName ?: "فایل $id"

    val humanSize: String
        get() = when {
            sizeBytes <= 0L -> ""
            sizeBytes < 1024L -> "$sizeBytes بایت"
            sizeBytes < 1024L * 1024L -> "${sizeBytes / 1024} کیلوبایت"
            else -> String.format("%.1f مگابایت", sizeBytes / (1024.0 * 1024.0))
        }

    /** مدت زمان ویدیو مثل «۱:۲۳» */
    val humanDuration: String?
        get() {
            val total = durationSeconds ?: return null
            if (total <= 0) return null
            return "%d:%02d".format(total / 60, total % 60)
        }

    /** آدرس مناسب برای نمایش تصویر: تصویر → stream، ویدیو → thumbnail */
    val previewUrl: String?
        get() = if (isVideo) thumbnailUrl else (streamUrl ?: downloadUrl)
}
