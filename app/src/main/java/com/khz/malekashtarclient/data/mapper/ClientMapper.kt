package com.khz.malekashtarclient.data.mapper

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.khz.malekashtarclient.data.dto.response.ChatContactDto
import com.khz.malekashtarclient.data.dto.response.ChatMessageDto
import com.khz.malekashtarclient.data.dto.response.ChatRoomDto
import com.khz.malekashtarclient.data.dto.response.ChatRoomMemberDto
import com.khz.malekashtarclient.data.dto.response.ClassScheduleItemDto
import com.khz.malekashtarclient.data.dto.response.MyChildBalanceDto
import com.khz.malekashtarclient.data.dto.response.MyChildClassDto
import com.khz.malekashtarclient.data.dto.response.MyChildDto
import com.khz.malekashtarclient.data.dto.response.MyClassDto
import com.khz.malekashtarclient.data.dto.response.MyFinanceDto
import com.khz.malekashtarclient.data.dto.response.MyInvoiceDto
import com.khz.malekashtarclient.data.dto.response.MyMatchDto
import com.khz.malekashtarclient.data.dto.response.MyScheduleDto
import com.khz.malekashtarclient.data.dto.response.NewsDto
import com.khz.malekashtarclient.domain.model.ChatContact
import com.khz.malekashtarclient.domain.model.ChatMessage
import com.khz.malekashtarclient.domain.model.ChatRoom
import com.khz.malekashtarclient.domain.model.ChatRoomMember
import com.khz.malekashtarclient.domain.model.MyChild
import com.khz.malekashtarclient.domain.model.MyChildBalance
import com.khz.malekashtarclient.domain.model.MyChildClass
import com.khz.malekashtarclient.domain.model.MyClass
import com.khz.malekashtarclient.domain.model.MyClassSchedule
import com.khz.malekashtarclient.domain.model.MyFinance
import com.khz.malekashtarclient.domain.model.MyInvoice
import com.khz.malekashtarclient.domain.model.MyMatch
import com.khz.malekashtarclient.domain.model.MyScheduleItem
import com.khz.malekashtarclient.domain.model.NewsItem

/**
 * تبدیل DTO → Domain Model برای ClientApi و ChatApi
 *
 * اصل: اگر فیلد ضروری nullable بود و null بود، مقدار پیش‌فرض ایمن می‌گذاریم.
 * اگر فیلد ناقص بود (مثل id پیام = null)، شیء کلاً ساخته نمی‌شود تا در UI خطا ندهیم.
 *
 * نکته: برای فیلدهایی مثل isLocked/isRead که سرور ممکن است bool/int بفرستد،
 * از AuthMapper.parseFlexibleBoolean الگو گرفته و درون همین‌جا تبدیل می‌کنیم.
 *
 * نکته مهم برای lastMessage: سرور آن را به‌صورت JSON object برمی‌گرداند.
 * در DTO ما Any? تعریف شده تا Gson خطا ندهد. در این mapper، آن را به ChatMessageDto
 * واقعی تبدیل می‌کنیم.
 */
object ClientMapper {

    private val gson = Gson()

    /* ═══════ helpers ═══════ */

    /**
     * تبدیل انعطاف‌پذیر Any? → Boolean
     * (بولی، عدد 0/1، یا رشته "true"/"false"/"1"/"0"/"yes"/"بله")
     */
    private fun parseFlexibleBoolean(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Boolean -> value
            is Int -> value != 0
            is Long -> value != 0L
            is Double -> value != 0.0
            is Float -> value != 0f
            is String -> when (value.trim()
                .lowercase()) {
                "true", "1", "yes", "بله", "on" -> true
                else                            -> false
            }

            else -> false
        }
    }

    /**
     * lastMessage ممکن است:
     *  - ChatMessageDto (Gson با موفقیت deserialize کرده) ← بعید ولی ممکن
     *  - Map<String, Any?> ← حالت واقعی وقتی DTO ما Any? دارد
     *  - JsonObject ← در حالت TypeAdapter
     * هر سه حالت را به ChatMessageDto تبدیل می‌کنیم.
     */
    private fun parseLastMessage(value: Any?): ChatMessageDto? {
        if (value == null) return null
        return when (value) {
            is ChatMessageDto -> value
            is JsonObject     -> gson.fromJson(
                value,
                ChatMessageDto::class.java
            )

            is Map<*, *>      -> {
                val obj = JsonObject()
                value.forEach { (k, v) ->
                    if (k != null && v != null) {
                        obj.add(
                            k.toString(),
                            gson.toJsonTree(v)
                        )
                    }
                }
                gson.fromJson(
                    obj,
                    ChatMessageDto::class.java
                )
            }

            else              -> null
        }
    }

    /* ═══════ me/children ═══════ */

    fun MyChildDto.toDomain(): MyChild? {
        val pid = id
                ?: return null
        return MyChild(
            id = pid,
            fullName = fullName?.takeIf { it.isNotBlank() }
                    ?: "بازیکن #$pid",
            birthDate = birthDate,
            age = age,
            gender = gender,
            nationalCode = nationalCode,
            avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
            currentClass = currentClass?.toDomain(),
            balance = balance?.toDomain()
        )
    }

    fun MyChildClassDto.toDomain(): MyChildClass? {
        val cid = id
                ?: return null
        return MyChildClass(
            id = cid,
            title = title?.takeIf { it.isNotBlank() }
                    ?: "کلاس #$cid",
            capacity = capacity,
            enrolledCount = enrolledCount,
            status = status)
    }

    fun MyChildBalanceDto.toDomain(): MyChildBalance = MyChildBalance(
        playerId = playerId
                ?: 0,
        debt = debt
                ?: 0L,
        totalPaid = totalPaid
                ?: 0L,
        pendingPaymentsCount = pendingPaymentsCount
                ?: 0
    )

    /* ═══════ me/schedule ═══════ */

    fun MyScheduleDto.toDomain(): MyScheduleItem? {
        val sid = id
                ?: return null
        return MyScheduleItem(
            id = sid,
            classId = classId
                    ?: 0,
            classTitle = classTitle?.takeIf { it.isNotBlank() }
                    ?: "کلاس",
            sessionDate = sessionDate
                    ?: "",
            startTime = startTime
                    ?: "",
            endTime = endTime,
            location = location,
            topic = topic,
            status = status,
            notes = notes)
    }

    /* ═══════ me/news ═══════ */

    fun NewsDto.toDomain(): NewsItem? {
        val nid = id
                ?: return null
        return NewsItem(
            id = nid,
            title = title?.takeIf { it.isNotBlank() }
                    ?: "(بدون عنوان)",
            body = body,
            publishedAt = publishedAt
                    ?: publishAt,
            createdAt = createdAt)
    }

    /* ═══════ me/classes ═══════ */

    fun MyClassDto.toDomain(): MyClass? {
        val cid = id
                ?: return null
        return MyClass(
            id = cid,
            title = title?.takeIf { it.isNotBlank() }
                    ?: "کلاس #$cid",
            ageGroupTitle = ageGroupTitle,
            coachName = coachName,
            coachId = coachId,
            assistantCoachName = assistantCoachName,
            location = location,
            description = description,
            capacity = capacity,
            enrolledCount = enrolledCount,
            schedules = schedules.mapNotNull { it.toDomain() })
    }

    fun ClassScheduleItemDto.toDomain(): MyClassSchedule? {
        val wd = weekday
                ?: return null
        return MyClassSchedule(
            weekday = wd,
            weekdayLabel = weekdayLabel?.takeIf { it.isNotBlank() }
                    ?: "",
            startTime = startTime
                    ?: "",
            endTime = endTime
                    ?: "",
            location = location)
    }

    /* ═══════ me/finance ═══════ */

    fun MyFinanceDto.toDomain(): MyFinance? {
        val pid = playerId
                ?: return null
        return MyFinance(
            playerId = pid,
            playerName = playerName?.takeIf { it.isNotBlank() }
                    ?: "بازیکن #$pid",
            totalInvoiced = totalInvoiced
                    ?: 0L,
            totalPaid = totalPaid
                    ?: 0L,
            balance = balance
                    ?: 0L,
            pendingPayments = pendingPayments
                    ?: 0,
            pendingAmount = pendingAmount
                    ?: 0L,
            invoices = invoices.mapNotNull { it.toDomain() })
    }

    fun MyInvoiceDto.toDomain(): MyInvoice? {
        val iid = id
                ?: return null
        return MyInvoice(
            id = iid,
            invoiceNumber = invoiceNumber,
            invoiceType = invoiceType,
            periodStartDate = periodStartDate,
            periodEndDate = periodEndDate,
            dueDate = dueDate,
            status = status,
            totalAmount = totalAmount
                    ?: 0L,
            paidAmount = paidAmount
                    ?: 0L,
            remainingAmount = remainingAmount
                    ?: 0L
        )
    }

    /* ═══════ me/matches ═══════ */

    fun MyMatchDto.toDomain(): MyMatch? {
        val mid = id
                ?: return null
        return MyMatch(
            id = mid,
            title = title,
            matchType = matchType,
            opponentTeam = opponentTeam,
            matchDate = matchDate
                    ?: "",
            matchTime = matchTime
                    ?: "",
            location = location,
            status = status,
            homeScore = homeScore,
            awayScore = awayScore,
            notes = notes,
            classTitle = classTitle,
            ageGroupTitle = ageGroupTitle
        )
    }

    /* ═══════ چت ═══════ */

    fun ChatContactDto.toDomain(): ChatContact? {
        val uid = userId
                ?: return null
        return ChatContact(
            userId = uid,
            fullName = fullName?.takeIf { it.isNotBlank() }
                    ?: "کاربر #$uid",
            role = role
                    ?: "admin",
            avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
            classTitle = classTitle
        )
    }

    fun ChatRoomDto.toDomain(): ChatRoom? {
        val rid = id
                ?: return null

        // lastMessage از سرور به‌صورت شیء است؛ تبدیل به ChatMessageDto
        val lastMsgDto = parseLastMessage(lastMessage)

        return ChatRoom(
            // ── فیلدهای اصلی سرور ──
            id = rid,
            roomType = roomType?.takeIf { it.isNotBlank() }
                    ?: "player_admin",
            status = status?.takeIf { it.isNotBlank() }
                    ?: "active",
            targetUserId = targetUserId,
            targetUserName = targetUserName,
            targetUserRole = targetUserRole,
            ageGroupId = ageGroupId,
            ageGroupTitle = ageGroupTitle,
            isLocked = parseFlexibleBoolean(isLocked),
            memberCount = memberCount
                    ?: 0,
            unreadCount = unreadCount
                    ?: 0,
            members = members.mapNotNull { it.toDomain() },

            // ── اختیاری/nullable ──
            playerId = playerId,
            classId = classId,
            subject = subject,
            createdAt = createdAt,
            updatedAt = updatedAt,

            // ── فیلدهای کمکی (extracted از last_message) ──
            lastMessageBody = lastMsgDto?.body?.takeIf { it.isNotBlank() },
            lastMessageAt = lastMsgDto?.sentAt
                    ?: lastMsgDto?.createdAt,
            lastMessageSenderName = lastMsgDto?.sender?.fullName,
            lastMessageSenderId = lastMsgDto?.senderId,

            // ── آینده‌نگر (فعلاً null) ──
            targetUserAvatar = targetUserAvatar?.takeIf { it.isNotBlank() },
            classTitle = classTitle?.takeIf { it.isNotBlank() })
    }

    fun ChatRoomMemberDto.toDomain(): ChatRoomMember? {
        val uid = userId
                ?: return null
        return ChatRoomMember(
            userId = uid,
            fullName = fullName?.takeIf { it.isNotBlank() }
                    ?: "عضو #$uid",
            role = role,
            memberRole = memberRole,
            lastReadMessageId = lastReadMessageId
            // avatar_url در hydrateRoom فرستاده نمی‌شود
        )
    }

    fun ChatMessageDto.toDomain(): ChatMessage? {
        val mid = id
                ?: return null
        return ChatMessage(
            id = mid,
            roomId = roomId
                    ?: 0,
            senderId = senderId,
            senderName = sender?.fullName,
            senderRole = sender?.role,
            senderAvatar = null,            // ← ChatMessageSenderDto در سرور avatar_url ندارد
            messageType = messageType
                    ?: "text",
            body = body
                    ?: "",
            isRead = parseFlexibleBoolean(isRead),
            createdAt = createdAt
                    ?: sentAt
        )
    }
}
