package com.khz.malekclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * پاسخ GET me/schedule → data.sessions[]
 *
 * پنجره: ۱۴ روز گذشته + آینده
 * ساعت‌ها "HH:MM:SS" — در UI فقط ۵ کاراکتر اول با DateUtils.formatTime
 * فیلدهای nullable (بعضی ممکن است اختیاری باشند)
 */
data class MyScheduleDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("class_title") val classTitle: String? = null,
    @SerializedName("session_date") val sessionDate: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("topic") val topic: String? = null,
    @SerializedName("status") val status: String? = null,           // scheduled / makeup / completed
    @SerializedName("notes") val notes: String? = null
)
