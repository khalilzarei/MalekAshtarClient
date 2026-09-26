package com.khz.malekclient.core.network

import com.google.gson.annotations.SerializedName

/**
 * پاکت استاندارد پاسخ همه‌ی endpointهای سرور:
 * {"success": bool, "message": string, "data": ...}
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

/** پاکت برای endpointهایی که data یک آبجکت ساده دارد */
data class SingleData<T>(
    @SerializedName("data") val data: T? = null
)

/** پاکت برای endpointهایی که data یک لیست است (مثل children، news، matches) */
data class ListData<T>(
    @SerializedName("data") val items: List<T> = emptyList()
)
