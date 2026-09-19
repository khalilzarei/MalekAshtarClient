package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * پاسخ GET me/classes → data.classes[]
 *
 * ساعت‌ها در schedules از قبل "HH:MM" هستند (نه "HH:MM:SS")
 */
data class MyClassDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("age_group_title") val ageGroupTitle: String? = null,
    @SerializedName("coach_name") val coachName: String? = null,
    @SerializedName("coach_id") val coachId: Int? = null,
    @SerializedName("assistant_coach_name") val assistantCoachName: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("capacity") val capacity: Int? = null,
    @SerializedName("enrolled_count") val enrolledCount: Int? = null,
    @SerializedName("schedules") val schedules: List<ClassScheduleItemDto> = emptyList()
)

/** آیتم برنامه‌ی هفتگی در me/classes */
data class ClassScheduleItemDto(
    @SerializedName("weekday") val weekday: Int? = null,          // 0 = شنبه (طبق پرامپت: weekday_label = "شنبه")
    @SerializedName("weekday_label") val weekdayLabel: String? = null,
    @SerializedName("start_time") val startTime: String? = null,  // "HH:MM"
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("location") val location: String? = null
)
