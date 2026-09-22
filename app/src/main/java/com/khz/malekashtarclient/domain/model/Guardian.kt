package com.khz.malekashtarclient.domain.model

data class Guardian(
    val id: Int,
    val fullName: String,
    val mobile: String?,
    val nationalCode: String?,
    val emergencyPhone: String?,
    val relation: String?,
    val isPrimary: Boolean
)

data class PlayerProfile(
    val user: UserInfo,
    val player: MyChild?,
    val guardians: List<Guardian>
)

data class UserInfo(
    val id: Int,
    val fullName: String,
    val mobile: String?,
    val nationalCode: String?,
    val avatarUrl: String?,
    val email: String?
)
