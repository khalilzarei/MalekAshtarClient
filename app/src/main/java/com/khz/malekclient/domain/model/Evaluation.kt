package com.khz.malekclient.domain.model

data class Evaluation(
    val id: Int,
    val playerId: Int,
    val sessionId: Int?,
    val classId: Int?,
    val classTitle: String?,
    val coachName: String?,
    val sessionDate: String?,
    val startTime: String?,
    val evaluationType: String?,
    val technicalScore: Int?,
    val disciplineScore: Int?,
    val physicalScore: Int?,
    val teamworkScore: Int?,
    val overallScore: Int?,
    val strengths: String?,
    val weaknesses: String?,
    val notes: String?,
    val createdAt: String?
) {
    val hasContent: Boolean
        get() = !strengths.isNullOrBlank() || !weaknesses.isNullOrBlank() || !notes.isNullOrBlank() || overallScore != null
}
