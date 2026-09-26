package com.khz.malekclient.data.dto.response

import com.google.gson.annotations.SerializedName

data class ChatContactDto(
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("class_title") val classTitle: String? = null
)

data class ChatContactsWrapperDto(
    @SerializedName("contacts")
    val contacts: List<ChatContactDto> = emptyList()
)
