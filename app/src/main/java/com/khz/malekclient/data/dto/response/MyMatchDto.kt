package com.khz.malekclient.data.dto.response

import com.google.gson.annotations.SerializedName

data class MyMatchDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("match_type") val matchType: String? = null,
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("age_group_id") val ageGroupId: Int? = null,
    @SerializedName("opponent_team") val opponentTeam: String? = null,
    @SerializedName("match_date") val matchDate: String? = null,
    @SerializedName("match_time") val matchTime: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("home_score") val homeScore: Int? = null,
    @SerializedName("away_score") val awayScore: Int? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("result") val result: String? = null,
    @SerializedName("class_title") val classTitle: String? = null,
    @SerializedName("age_group_title") val ageGroupTitle: String? = null,
    // اطلاعات دعوت بازیکن جاری
    @SerializedName("invitation_status") val invitationStatus: String? = null,
    @SerializedName("attendance_status") val attendanceStatus: String? = null,
    @SerializedName("jersey_number") val jerseyNumber: Int? = null,
    @SerializedName("position") val position: String? = null,
    @SerializedName("goals") val goals: Int? = null,
    @SerializedName("assists") val assists: Int? = null,
    @SerializedName("yellow_cards") val yellowCards: Int? = null,
    @SerializedName("red_cards") val redCards: Int? = null,
    @SerializedName("minutes_played") val minutesPlayed: Int? = null,
    @SerializedName("rating") val rating: Float? = null,
    @SerializedName("player_notes") val playerNotes: String? = null
)
