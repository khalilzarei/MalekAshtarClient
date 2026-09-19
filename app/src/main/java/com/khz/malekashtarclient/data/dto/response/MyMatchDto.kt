package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * پاسخ GET me/matches → data.matches[]
 *
 * status: planned | completed | cancelled | ...
 * نتیجه فقط وقتی completed پر است — home_score/away_score ممکن است null باشند
 * ساعت "HH:MM:SS"
 */
data class MyMatchDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("match_type") val matchType: String? = null,        // friendly | ...
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("age_group_id") val ageGroupId: Int? = null,
    @SerializedName("opponent_team") val opponentTeam: String? = null,
    @SerializedName("match_date") val matchDate: String? = null,        // YYYY-MM-DD
    @SerializedName("match_time") val matchTime: String? = null,        // HH:MM:SS
    @SerializedName("location") val location: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("home_score") val homeScore: Int? = null,
    @SerializedName("away_score") val awayScore: Int? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("class_title") val classTitle: String? = null,
    @SerializedName("age_group_title") val ageGroupTitle: String? = null
)
