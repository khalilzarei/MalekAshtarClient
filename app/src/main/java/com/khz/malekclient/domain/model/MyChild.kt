package com.khz.malekclient.domain.model

/**
 * بازیکن مرتبط با این حساب (از me/children)
 */
data class MyChild(
    val id: Int,
    val fullName: String,
    val birthDate: String?,         // YYYY-MM-DD میلادی
    val age: Int?,
    val gender: String?,
    val nationalCode: String?,
    val avatarUrl: String?,
    val currentClass: MyChildClass?,
    val balance: MyChildBalance?
)

data class MyChildClass(
    val id: Int,
    val title: String,
    val ageGroupTitle: String?,
    val capacity: Int?,
    val enrolledCount: Int?,
    val status: String?
)

data class MyChildBalance(
    val playerId: Int,
    val debt: Long,
    val totalPaid: Long,
    val pendingPaymentsCount: Int
)
