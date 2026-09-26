package com.khz.malekclient.domain.model

data class Guardian(
    val id: Int,
    val fullName: String,
    val mobile: String?,
    val nationalCode: String?,
    val emergencyPhone: String?,
    val relation: String?,
    val isPrimary: Boolean
) {
    /** برچسب فارسی نسبت — هم‌راستا با اپ ادمین */
    val relationLabel: String
        get() = when (relation) {
            "father"      -> "پدر"
            "mother"      -> "مادر"
            "grandfather" -> "پدربزرگ"
            "grandmother" -> "مادربزرگ"
            "uncle"       -> "عمو/دایی"
            "aunt"        -> "عمه/خاله"
            "other"       -> "سایر"
            else          -> relation?.ifBlank { "—" }
                    ?: "—"
        }

    companion object {
        /** گزینه‌های نسبت: (کلید انگلیسی برای ذخیره / برچسب فارسی برای نمایش) */
        val relationOptions: List<Pair<String, String>> = listOf(
            "father" to "پدر",
            "mother" to "مادر",
            "grandfather" to "پدربزرگ",
            "grandmother" to "مادربزرگ",
            "uncle" to "عمو/دایی",
            "aunt" to "عمه/خاله",
            "other" to "سایر"
        )
    }
}

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
