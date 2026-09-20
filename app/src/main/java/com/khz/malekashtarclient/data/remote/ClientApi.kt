package com.khz.malekashtarclient.data.remote

import com.khz.malekashtarclient.core.network.ApiResponse
import com.khz.malekashtarclient.data.dto.response.MyChildDto
import com.khz.malekashtarclient.data.dto.response.MyClassDto
import com.khz.malekashtarclient.data.dto.response.MyFinanceDto
import com.khz.malekashtarclient.data.dto.response.MyMatchDto
import com.khz.malekashtarclient.data.dto.response.MyScheduleDto
import com.khz.malekashtarclient.data.dto.response.NewsDto
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ClientApi {

    /** GET me/children → آرایه‌ی بازیکن(های) متصل به این حساب (معمولاً ۰ یا ۱) */
    @GET("me/children")
    suspend fun myChildren(): ApiResponse<MyChildrenWrapperDto>

    /** GET me/schedule → جلسات ۱۴ روز اخیر و آینده */
    @GET("me/schedule")
    suspend fun mySchedule(): ApiResponse<MyScheduleWrapperDto>

    /** GET me/news → اخبار منتشرشده‌ی مجاز برای این کاربر */
    @GET("me/news")
    suspend fun myNews(): ApiResponse<NewsWrapperDto>

    /** GET me/classes → کلاس‌های فعال بازیکن */
    @GET("me/classes")
    suspend fun myClasses(): ApiResponse<MyClassesWrapperDto>

    /** GET me/finance → صورت حساب بازیکن */
    @GET("me/finance")
    suspend fun myFinance(): ApiResponse<MyFinanceWrapperDto>

    /** GET me/matches → مسابقات کلاس/گروه سنی بازیکن */
    @GET("me/matches")
    suspend fun myMatches(): ApiResponse<MyMatchesWrapperDto>

    /** GET me/chat-contacts → لیست مخاطبین قابل گفتگو */
    @GET("me/chat-contacts")
    suspend fun myChatContacts(): ApiResponse<ChatContactsWrapperDto>

    /** POST me/avatar → آپلود عکس پروفایل (multipart، فیلد "avatar") */
    @Multipart
    @POST("me/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): ApiResponse<Any?>

    /** DELETE me/avatar → حذف عکس پروفایل */
    @DELETE("me/avatar")
    suspend fun deleteAvatar(): ApiResponse<Any?>
}

/* ═══════════════ Wrapper DTOs ═══════════════ */

/** پاکت me/children: data.children = [...] */
data class MyChildrenWrapperDto(
    val children: List<MyChildDto> = emptyList()
)

/** پاکت me/schedule: data.sessions = [...] */
data class MyScheduleWrapperDto(
    val sessions: List<MyScheduleDto> = emptyList()
)

/** پاکت me/news: data.news = [...] */
data class NewsWrapperDto(
    val news: List<NewsDto> = emptyList()
)

/** پاکت me/classes: data.classes = [...] */
data class MyClassesWrapperDto(
    val classes: List<MyClassDto> = emptyList()
)

/** پاکت me/finance: data.finance = [...] */
data class MyFinanceWrapperDto(
    val finance: List<MyFinanceDto> = emptyList()
)

/** پاکت me/matches: data.matches = [...] */
data class MyMatchesWrapperDto(
    val matches: List<MyMatchDto> = emptyList()
)

/** پاکت me/chat-contacts: data.contacts = [...] */
data class ChatContactsWrapperDto(
    val contacts: List<com.khz.malekashtarclient.data.dto.response.ChatContactDto> = emptyList()
)
