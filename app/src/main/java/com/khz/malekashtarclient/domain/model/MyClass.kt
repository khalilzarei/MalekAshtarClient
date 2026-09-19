package com.khz.malekashtarclient.domain.model

/**
 * یک کلاس فعال بازیکن (از me/classes)
 */
data class MyClass(
    val id: Int,
    val title: String,
    val ageGroupTitle: String?,
    val coachName: String?,
    val coachId: Int?,
    val assistantCoachName: String?,
    val location: String?,
    val description: String?,
    val capacity: Int?,
    val enrolledCount: Int?,
    val schedules: List<MyClassSchedule>
)

/** آیتم برنامه‌ی هفتگی */
data class MyClassSchedule(
    val weekday: Int,
    val weekdayLabel: String,
    val startTime: String,          // "HH:MM" (در این endpoint از قبل کوتاه است)
    val endTime: String,
    val location: String?
)
