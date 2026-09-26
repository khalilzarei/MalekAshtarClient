package com.khz.malekclient.domain.model

data class MyClass(
    val id: Int,
    val title: String,
    val ageGroupTitle: String?,
    val coachName: String?,
    val coachId: Int?,
    val coachUserId: Int?,
    val coachAvatarUrl: String?,
    val assistantCoachName: String?,
    val location: String?,
    val description: String?,
    val capacity: Int?,
    val enrolledCount: Int?,
    val schedules: List<MyClassSchedule>
)

data class MyClassSchedule(
    val weekday: Int,
    val weekdayLabel: String,
    val startTime: String,
    val endTime: String,
    val location: String?
)

