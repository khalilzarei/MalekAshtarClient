package com.khz.malekashtarclient.domain.model

/**
 * یک مسابقه (از me/matches)
 */
data class MyMatch(
    val id: Int,
    val title: String?,
    val matchType: String?,             // friendly | ...
    val opponentTeam: String?,
    val matchDate: String,              // YYYY-MM-DD
    val matchTime: String,              // "HH:MM:SS" — در UI با take(5)
    val location: String?,
    val status: String?,                // planned | completed | cancelled
    val homeScore: Int?,
    val awayScore: Int?,
    val notes: String?,
    val classTitle: String?,
    val ageGroupTitle: String?
) {
    /** آیا نتیجه ثبت شده (status == completed و هر دو امتیاز موجود) */
    val hasResult: Boolean
        get() = status == "completed" && homeScore != null && awayScore != null

    /** نتیجه به شکل "X - Y" یا null */
    val resultText: String?
        get() = if (hasResult) "${homeScore} - ${awayScore}" else null
}
