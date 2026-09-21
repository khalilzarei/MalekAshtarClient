package com.khz.malekashtarclient.data.repository

import com.khz.malekashtarclient.core.network.ApiErrorHandler
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.data.dto.response.ChatContactDto
import com.khz.malekashtarclient.data.mapper.ClientMapper.toDomain
import com.khz.malekashtarclient.data.remote.ClientApi
import com.khz.malekashtarclient.domain.model.ChatContact
import com.khz.malekashtarclient.domain.model.MyChild
import com.khz.malekashtarclient.domain.model.MyClass
import com.khz.malekashtarclient.domain.model.MyFinance
import com.khz.malekashtarclient.domain.model.MyMatch
import com.khz.malekashtarclient.domain.model.MyScheduleItem
import com.khz.malekashtarclient.domain.model.NewsItem
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ClientRepository(
    private val api: ClientApi
) {

    /** بازیکن/بازیکنان مرتبط با این حساب */
    suspend fun myChildren(): NetworkResult<List<MyChild>> {
        return try {
            val response = api.myChildren()

            if (!response.success) {
                NetworkResult.Error(
                    response.message
                            ?: "خطا در دریافت اطلاعات بازیکن"
                )
            } else {
                val list = response.data?.children?.mapNotNull { it.toDomain() }
                        ?: emptyList()

                NetworkResult.Success(list)
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /** جلسات ۱۴ روز اخیر و آینده */
    suspend fun mySchedule(): NetworkResult<List<MyScheduleItem>> {
        return try {
            val response = api.mySchedule()

            if (!response.success) {
                NetworkResult.Error(
                    response.message
                            ?: "خطا در دریافت برنامه‌ی جلسات"
                )
            } else {
                val list = response.data?.sessions?.mapNotNull { it.toDomain() }
                        ?: emptyList()

                NetworkResult.Success(list)
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /** اخبار مجاز برای این کاربر */
    suspend fun myNews(): NetworkResult<List<NewsItem>> {
        return try {
            val response = api.myNews()

            if (!response.success) {
                NetworkResult.Error(
                    response.message
                            ?: "خطا در دریافت اخبار"
                )
            } else {
                val list = response.data?.news?.mapNotNull { it.toDomain() }
                        ?: emptyList()

                NetworkResult.Success(list)
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /** کلاس‌های فعال بازیکن */
    suspend fun myClasses(): NetworkResult<List<MyClass>> {
        return try {
            val response = api.myClasses()

            if (!response.success) {
                NetworkResult.Error(
                    response.message
                            ?: "خطا در دریافت کلاس‌ها"
                )
            } else {
                val list = response.data?.classes?.mapNotNull { it.toDomain() }
                        ?: emptyList()

                NetworkResult.Success(list)
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /**
     * مخاطبین قابل گفتگو (ادمین‌ها + مربیان کلاس‌های فرزندان).
     * بازیکن با هر یک از آن‌ها می‌تواند گفتگوی خصوصی شروع کند.
     */
    suspend fun chatContacts(): NetworkResult<List<ChatContactDto>?> {
        return try {
            val response = api.chatContacts()

            if (!response.success) {
                NetworkResult.Error(
                    response.message
                            ?: "خطا در دریافت مخاطبین گفتگو"
                )
            } else {
                val list = response.data?.contacts
                        ?: emptyList()

                NetworkResult.Success(list)
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /** صورت‌حساب بازیکن */
    suspend fun myFinance(): NetworkResult<List<MyFinance>> {
        return try {
            val response = api.myFinance()

            if (!response.success) {
                NetworkResult.Error(
                    response.message
                            ?: "خطا در دریافت صورت حساب"
                )
            } else {
                val list = response.data?.finance?.mapNotNull { it.toDomain() }
                        ?: emptyList()

                NetworkResult.Success(list)
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /** مسابقات */
    suspend fun myMatches(): NetworkResult<List<MyMatch>> {
        return try {
            val response = api.myMatches()

            if (!response.success) {
                NetworkResult.Error(
                    response.message
                            ?: "خطا در دریافت مسابقات"
                )
            } else {
                val list = response.data?.matches?.mapNotNull { it.toDomain() }
                        ?: emptyList()

                NetworkResult.Success(list)
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /** آپلود آواتار */
    suspend fun uploadAvatar(
        file: File
    ): NetworkResult<Unit> {
        return try {
            val body = file.asRequestBody(
                "image/*".toMediaTypeOrNull()
            )

            val part = MultipartBody.Part.createFormData(
                "avatar",
                file.name,
                body
            )

            val response = api.uploadAvatar(part)

            if (!response.success) {
                NetworkResult.Error(
                    response.message
                            ?: "خطا در آپلود عکس"
                )
            } else {
                NetworkResult.Success(Unit)
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                ApiErrorHandler.extractMessage(e)
            )
        }
    }

    /** حذف آواتار */
    suspend fun deleteAvatar(): NetworkResult<Unit> {
        return try {
            val response = api.deleteAvatar()

            if (!response.success) {
                NetworkResult.Error(
                    response.message
                            ?: "خطا در حذف عکس"
                )
            } else {
                NetworkResult.Success(Unit)
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                ApiErrorHandler.extractMessage(e)
            )
        }
    }
}