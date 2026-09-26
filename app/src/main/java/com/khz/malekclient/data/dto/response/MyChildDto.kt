package com.khz.malekclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * پاسخ GET me/children → data.children[0]
 *
 * current_class و balance ممکن است null باشند (بازیکن بدون ثبت‌نام/فاکتور)
 */
data class MyChildDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("birth_date") val birthDate: String? = null,
    @SerializedName("age") val age: Int? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("national_code") val nationalCode: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("current_class") val currentClass: MyChildClassDto? = null,
    @SerializedName("balance") val balance: MyChildBalanceDto? = null
)

/** اطلاعات کلاس فعلی بازیکن */
data class MyChildClassDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("age_group_title") val ageGroupTitle: String? = null,
    @SerializedName("season_id") val seasonId: Int? = null,
    @SerializedName("age_group_id") val ageGroupId: Int? = null,
    @SerializedName("coach_id") val coachId: Int? = null,
    @SerializedName("assistant_coach_id") val assistantCoachId: Int? = null,
    @SerializedName("capacity") val capacity: Int? = null,
    @SerializedName("enrolled_count") val enrolledCount: Int? = null,
    @SerializedName("status") val status: String? = null
)

/** اطلاعات مالی بازیکن در کارت آیتم me/children */
data class MyChildBalanceDto(
    @SerializedName("player_id") val playerId: Int? = null,
    @SerializedName("debt") val debt: Long? = null,
    @SerializedName("total_paid") val totalPaid: Long? = null,
    @SerializedName("pending_payments_count") val pendingPaymentsCount: Int? = null
)
