package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

data class EvaluationDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("player_id") val playerId: Int? = null,
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("class_title") val classTitle: String? = null,
    @SerializedName("coach_name") val coachName: String? = null,
    @SerializedName("session_date") val sessionDate: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("evaluation_type") val evaluationType: String? = null,
    @SerializedName("technical_score") val technicalScore: Int? = null,
    @SerializedName("discipline_score") val disciplineScore: Int? = null,
    @SerializedName("physical_score") val physicalScore: Int? = null,
    @SerializedName("teamwork_score") val teamworkScore: Int? = null,
    @SerializedName("overall_score") val overallScore: Int? = null,
    @SerializedName("strengths") val strengths: String? = null,
    @SerializedName("weaknesses") val weaknesses: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class EvaluationsWrapperDto(
    @SerializedName("evaluations") val evaluations: List<EvaluationDto> = emptyList()
)
