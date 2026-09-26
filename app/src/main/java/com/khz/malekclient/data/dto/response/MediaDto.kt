package com.khz.malekclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * پاسخ سرور برای رسانه (عکس یا فیلم).
 *
 * سرور از `MediaService::present()` استفاده می‌کند و این فیلدها را می‌فرستد:
 *  - url          → آدرس دانلود (Content-Disposition: attachment)
 *  - stream_url   → آدرس پخش درون‌خطی (inline + پشتیبانی Range)
 *  - thumbnail_url → تصویر پیش‌نمایش (اگر برای ویدیو ساخته شده باشد)
 *
 * همه‌ی فیلدها nullable با مقدار پیش‌فرض‌اند تا نبود یک فیلد باعث crash نشود.
 */
data class MediaDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("file_name") val fileName: String? = null,
    @SerializedName("original_name") val originalName: String? = null,
    @SerializedName("file_type") val fileType: String? = null,
    @SerializedName("file_size") val fileSize: Long = 0L,
    @SerializedName("mime_type") val mimeType: String? = null,
    @SerializedName("duration_seconds") val durationSeconds: Int? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("stream_url") val streamUrl: String? = null,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
