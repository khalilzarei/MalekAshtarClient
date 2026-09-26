package com.khz.malekclient.data.remote

import com.khz.malekclient.core.network.ApiResponse
import com.khz.malekclient.data.dto.response.*
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

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

    /** GET me/news/{id} */
    @GET("me/news/{id}")
    suspend fun myNewsDetail(@Path("id") id: Int): ApiResponse<NewsDetailWrapperDto>

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

    /** GET me/profile */
    @GET("me/profile")
    suspend fun myProfile(): ApiResponse<ProfileWrapperDto>

    /** PUT me/profile */
    @PUT("me/profile")
    suspend fun updateProfile(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiResponse<ProfileWrapperDto>

    /** PUT me/children/{id} */
    @PUT("me/children/{id}")
    suspend fun updateChild(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): ApiResponse<ProfileWrapperDto>

    /** GET me/guardians */
    @GET("me/guardians")
    suspend fun myGuardians(): ApiResponse<GuardiansWrapperDto>

    /** PUT me/guardians/{id} */
    @PUT("me/guardians/{id}")
    suspend fun updateGuardian(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): ApiResponse<ProfileWrapperDto>

    /** GET me/evaluations */
    @GET("me/evaluations")
    suspend fun myEvaluations(): ApiResponse<EvaluationsWrapperDto>
}
