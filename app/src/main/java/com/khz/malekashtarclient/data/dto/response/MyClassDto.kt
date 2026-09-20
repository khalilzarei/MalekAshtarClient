package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

data class MyClassDto(
    @SerializedName("id") val id: Int? = null,

    @SerializedName("title") val title: String? = null,

    @SerializedName("age_group_title") val ageGroupTitle: String? = null,

    @SerializedName("coach_name") val coachName: String? = null,

    @SerializedName("coach_id") val coachId: Int? = null,

    @SerializedName("coach_user_id") val coachUserId: Int? = null,

    @SerializedName("coach_avatar_url") val coachAvatarUrl: String? = null,

    @SerializedName("assistant_coach_name") val assistantCoachName: String? = null,

    @SerializedName("location") val location: String? = null,

    @SerializedName("description") val description: String? = null,

    @SerializedName("capacity") val capacity: Int? = null,

    @SerializedName("enrolled_count") val enrolledCount: Int? = null,

    @SerializedName("schedules") val schedules: List<ClassScheduleItemDto> = emptyList()
)

data class ClassScheduleItemDto(
    @SerializedName("weekday") val weekday: Int? = null,

    @SerializedName("weekday_label") val weekdayLabel: String? = null,

    @SerializedName("start_time") val startTime: String? = null,

    @SerializedName("end_time") val endTime: String? = null,

    @SerializedName("location") val location: String? = null
)
