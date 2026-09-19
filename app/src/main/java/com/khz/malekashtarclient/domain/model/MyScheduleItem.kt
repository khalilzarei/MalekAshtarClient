package com.khz.malekashtarclient.domain.model

/**
 * یک جلسه‌ی آموزشی (از me/schedule)
 *
 * تاریخ‌ها میلادی هستند (نمایش با DateUtils شمسی می‌شود).
 */
data class MyScheduleItem(
    val id: Int,
    val classId: Int,
    val classTitle: String,
    val sessionDate: String,        // YYYY-MM-DD
    val startTime: String,          // "HH:MM:SS" — در UI با take(5) کوتاه شود
    val endTime: String?,
    val location: String?,
    val topic: String?,
    val status: String?,            // scheduled / makeup / completed
    val notes: String?
)
