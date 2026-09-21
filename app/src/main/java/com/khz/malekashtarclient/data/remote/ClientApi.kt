package com.khz.malekashtarclient.data.remote

import com.khz.malekashtarclient.core.network.ApiResponse
import com.khz.malekashtarclient.data.dto.response.*
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ClientApi {

    /** GET me/children */
    @GET("me/children")
    suspend fun myChildren(): ApiResponse<MyChildrenWrapperDto>

    /** GET me/schedule */
    @GET("me/schedule")
    suspend fun mySchedule(): ApiResponse<MyScheduleWrapperDto>

    /** GET me/news */
    @GET("me/news")
    suspend fun myNews(): ApiResponse<NewsWrapperDto>

    /** GET me/classes */
    @GET("me/classes")
    suspend fun myClasses(): ApiResponse<MyClassesWrapperDto>

    /** GET me/finance */
    @GET("me/finance")
    suspend fun myFinance(): ApiResponse<MyFinanceWrapperDto>

    /** GET me/matches */
    @GET("me/matches")
    suspend fun myMatches(): ApiResponse<MyMatchesWrapperDto>

    /**
     * GET me/chat-contacts
     * مخاطبین قابل گفتگو (ادمین‌ها + مربیان کلاس‌های فرزندان)
     */
    @GET("me/chat-contacts")
    suspend fun chatContacts(): ApiResponse<ChatContactsWrapperDto>

    /** POST me/avatar */
    @Multipart
    @POST("me/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): ApiResponse<Any?>

    /** DELETE me/avatar */
    @DELETE("me/avatar")
    suspend fun deleteAvatar(): ApiResponse<Any?>
}
