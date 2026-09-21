package com.khz.malekashtarclient.data.mapper

import com.khz.malekashtarclient.data.dto.response.*
import com.khz.malekashtarclient.domain.model.*

object ClientMapper {

    /* ═══════════════════════════════════════
       Boolean
       ═══════════════════════════════════════ */

    private fun parseFlexibleBoolean(value: Any?): Boolean {
        return when (value) {
            null -> false

            is Boolean -> value

            is Int -> value != 0

            is Long -> value != 0L

            is Double -> value != 0.0

            is Float -> value != 0f

            is String -> {
                when (value.trim()
                    .lowercase()) {
                    "true", "1", "yes", "بله", "on" -> true

                    else                            -> false
                }
            }

            else -> false
        }
    }

    /* ═══════════════════════════════════════
       me / children
       ═══════════════════════════════════════ */

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

    /* ═══════════════════════════════════════
       me / schedule
       ═══════════════════════════════════════ */

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

    /* ═══════════════════════════════════════
       me / news
       ═══════════════════════════════════════ */

    /**
     * یک رسانه‌ی خبر.
     * نوع فایل از file_type می‌آید و اگر سرور آن را نفرستد، از روی MIME
     * استنتاج می‌شود.
     */
    fun MediaDto.toDomain(): NewsMedia? {
        if (id <= 0) return null

        val type = fileType
                ?: mimeType?.substringBefore('/')

        return NewsMedia(
            id = id,
            fileName = fileName,
            originalName = originalName,
            isVideo = type == "video",
            mimeType = mimeType
                    ?: "application/octet-stream",
            sizeBytes = fileSize,
            durationSeconds = durationSeconds,
            downloadUrl = url,
            streamUrl = streamUrl
                    ?: url,
            thumbnailUrl = thumbnailUrl,
            description = description
        )
    }

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
            createdAt = createdAt,
            media = media?.mapNotNull { it.toDomain() }
                    ?: emptyList()
        )
    }

    /* ═══════════════════════════════════════
       me / classes
       ═══════════════════════════════════════ */

    fun ClassScheduleItemDto.toDomain(): MyClassSchedule {
        return MyClassSchedule(
            weekday = weekday
                    ?: 0,
            weekdayLabel = weekdayLabel.orEmpty(),
            startTime = startTime.orEmpty(),
            endTime = endTime.orEmpty(),
            location = location
        )
    }

    fun MyClassDto.toDomain(): MyClass {
        return MyClass(
            id = id
                    ?: 0,
            title = title.orEmpty(),
            ageGroupTitle = ageGroupTitle,
            coachName = coachName,
            coachId = coachId,
            coachUserId = coachUserId,
            coachAvatarUrl = coachAvatarUrl,
            assistantCoachName = assistantCoachName,
            location = location,
            description = description,
            capacity = capacity,
            enrolledCount = enrolledCount,
            schedules = schedules.map { it.toDomain() })
    }

    /* ═══════════════════════════════════════
       me / finance
       ═══════════════════════════════════════ */

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
            invoices = invoices.mapNotNull {
                it.toDomain()
            })
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

    /* ═══════════════════════════════════════
       me / matches
       ═══════════════════════════════════════ */

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

    /* ═══════════════════════════════════════
       Chat
       ═══════════════════════════════════════ */

    fun ChatRoomMemberDto.toDomain(): ChatRoomMember? {
        val uid = userId
                ?: return null

        return ChatRoomMember(
            userId = uid,
            fullName = fullName?.takeIf { it.isNotBlank() }
                    ?: "عضو #$uid",
            role = role,
            memberRole = memberRole,
            lastReadMessageId = lastReadMessageId)
    }

    fun ChatRoomDto.toDomain(): ChatRoom {
        return ChatRoom(
            id = id
                    ?: 0,

            /**
             * is_group تنها مرجع تشخیص private/group است.
             * تعداد اعضا ملاک نیست.
             */
            isGroup = isGroup
                    ?: false,

            title = title.orEmpty(),

            image = image,

            users = users.map {
                ChatRoomUser(
                    id = it.id
                            ?: 0,
                    fullName = it.fullName.orEmpty(),
                    avatar = it.avatar,
                    role = it.role,
                    memberRole = it.memberRole
                )
            },

            unreadCount = unreadCount
                    ?: 0,

            lastMessage = lastMessage?.toDomain(),

            status = status
                    ?: "active",

            isLocked = parseFlexibleBoolean(isLocked),

            /**
             * این دو فقط metadata هستند.
             * برای تعیین طرف چت استفاده نمی‌شوند.
             */
            playerId = playerId,
            classId = classId,

            subject = subject,

            createdAt = createdAt,

            updatedAt = updatedAt
        )
    }

    fun ChatMessageDto.toDomain(): ChatMessage {
        return ChatMessage(
            id = id
                ?: 0,

            roomId = roomId
                    ?: 0,

            senderId = senderId
                    ?: sender?.id,

            senderName = sender?.fullName,

            senderRole = sender?.role,

            senderAvatar = sender?.avatarUrl?.takeIf { it.isNotBlank() }
                    ?: sender?.avatar?.takeIf { it.isNotBlank() },

            messageType = messageType
                    ?: "text",

            body = body.orEmpty(),

            isRead = parseFlexibleBoolean(isRead),

            createdAt = createdAt
                    ?: sentAt
        )
    }
}