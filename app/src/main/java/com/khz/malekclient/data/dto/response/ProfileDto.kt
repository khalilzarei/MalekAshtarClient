package com.khz.malekclient.data.dto.response

import com.google.gson.annotations.SerializedName

data class ProfileWrapperDto(
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("player") val player: MyChildDto? = null,
    @SerializedName("guardians") val guardians: List<GuardianDto> = emptyList()
)

data class GuardianDto(
    @SerializedName("guardian_id") val guardianId: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("user_full_name") val userFullName: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("user_mobile") val userMobile: String? = null,
    @SerializedName("national_code") val nationalCode: String? = null,
    @SerializedName("user_national_code") val userNationalCode: String? = null,
    @SerializedName("emergency_phone") val emergencyPhone: String? = null,
    @SerializedName("relation") val relation: String? = null,
    @SerializedName("is_primary") val isPrimary: Any? = null
)

data class GuardiansWrapperDto(
    @SerializedName("guardians") val guardians: List<GuardianDto> = emptyList()
)
