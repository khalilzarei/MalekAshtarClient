package com.khz.malekashtarclient.domain.model

data class MyMatch(
    val id: Int,
    val title: String?,
    val matchType: String?,
    val opponentTeam: String?,
    val matchDate: String,
    val matchTime: String,
    val location: String?,
    val status: String?,
    val homeScore: Int?,
    val awayScore: Int?,
    val notes: String?,
    val result: String?,
    val classTitle: String?,
    val ageGroupTitle: String?,
    val invitationStatus: String?,
    val attendanceStatus: String?,
    val jerseyNumber: Int?,
    val position: String?,
    val goals: Int?,
    val assists: Int?,
    val yellowCards: Int?,
    val redCards: Int?,
    val minutesPlayed: Int?,
    val rating: Float?,
    val playerNotes: String?
) {
    val hasResult: Boolean
        get() = status == "completed" && homeScore != null && awayScore != null

    val resultText: String?
        get() = if (hasResult) "${homeScore} - ${awayScore}" else result

    val isInvited: Boolean
        get() = invitationStatus != null

    val isStarter: Boolean
        get() = invitationStatus == "invited" || invitationStatus == "accepted"
}
