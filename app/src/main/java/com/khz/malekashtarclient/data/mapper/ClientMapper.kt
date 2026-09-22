package com.khz.malekashtarclient.data.mapper

import com.khz.malekashtarclient.data.dto.response.*
import com.khz.malekashtarclient.domain.model.*

object ClientMapper {

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
            ageGroupTitle = ageGroupTitle?.takeIf { it.isNotBlank() },
            capacity = capacity,
            enrolledCount = enrolledCount,
            status = status
        )
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
                    ?: emptyList())
    }

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

    fun MyFinanceDto.toDomain(): MyFinance? {
        val pid = playerId
                ?: return null
        val bal = balance
                ?: debt
                ?: 0L
        val isDebt = isDebtor
                ?: (bal > 0)
        return MyFinance(
            playerId = pid,
            playerName = playerName?.takeIf { it.isNotBlank() }
                    ?: "بازیکن #$pid",
            totalInvoiced = totalInvoiced
                    ?: 0L,
            totalPaid = totalPaid
                    ?: 0L,
            balance = bal,
            debt = debt
                    ?: bal,
            isDebtor = isDebt,
            pendingPayments = pendingPayments
                    ?: 0,
            pendingAmount = pendingAmount
                    ?: 0L,
            invoices = invoices.mapNotNull { it.toDomain() },
            classDebts = classDebts.mapNotNull { it.toDomain() },
            classFees = classFees.mapNotNull { it.toDomain() })
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
            subtotal = subtotal
                    ?: totalAmount
                    ?: 0L,
            discountTotal = discountTotal
                    ?: 0L,
            totalAmount = totalAmount
                    ?: 0L,
            paidAmount = paidAmount
                    ?: 0L,
            remainingAmount = remainingAmount
                    ?: 0L,
            notes = notes,
            items = items.mapNotNull { it.toDomain() })
    }

    fun InvoiceItemDto.toDomain(): InvoiceItem? {
        val iid = id
                ?: return null
        return InvoiceItem(
            id = iid,
            title = title
                    ?: "آیتم #$iid",
            itemType = itemType,
            amount = amount
                    ?: 0L,
            quantity = quantity
                    ?: 1,
            total = total
                    ?: 0L,
            classId = classId,
            classTitle = classTitle,
            ageGroupTitle = ageGroupTitle,
            description = description
        )
    }

    fun ClassDebtDto.toDomain(): ClassDebt? {
        return ClassDebt(
            classId = classId,
            classTitle = classTitle
                    ?: "کلاس",
            ageGroupTitle = ageGroupTitle,
            total = total
                    ?: 0L,
            paid = paid
                    ?: 0L,
            remaining = remaining
                    ?: 0L,
            itemsCount = itemsCount
                    ?: 0
        )
    }

    fun ClassFeeDto.toDomain(): ClassFee? {
        return ClassFee(
            classId = classId,
            classTitle = classTitle
                    ?: "کلاس",
            ageGroupTitle = ageGroupTitle,
            monthlyFee = monthlyFee,
            sessionFee = sessionFee,
            registrationFee = registrationFee,
            enrolled = enrolled
                    ?: false,
            debt = debt
                    ?: 0L,
            paid = paid
                    ?: 0L,
            total = total
                    ?: 0L
        )
    }

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
            result = result,
            classTitle = classTitle,
            ageGroupTitle = ageGroupTitle,
            invitationStatus = invitationStatus,
            attendanceStatus = attendanceStatus,
            jerseyNumber = jerseyNumber,
            position = position,
            goals = goals,
            assists = assists,
            yellowCards = yellowCards,
            redCards = redCards,
            minutesPlayed = minutesPlayed,
            rating = rating,
            playerNotes = playerNotes
        )
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
            lastReadMessageId = lastReadMessageId)
    }

    fun ChatRoomDto.toDomain(): ChatRoom {
        return ChatRoom(
            id = id
                    ?: 0,
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

    fun GuardianDto.toDomain(): Guardian? {
        val gid = guardianId
                ?: id
                ?: return null
        return Guardian(
            id = gid,
            fullName = (userFullName
                    ?: fullName)?.takeIf { it.isNotBlank() }
                    ?: "سرپرست #$gid",
            mobile = userMobile
                    ?: mobile,
            nationalCode = userNationalCode
                    ?: nationalCode,
            emergencyPhone = emergencyPhone,
            relation = relation,
            isPrimary = parseFlexibleBoolean(isPrimary))
    }

    fun UserDto.toUserInfo(): UserInfo {
        return UserInfo(
            id = id
                ?: 0,
            fullName = fullName?.takeIf { it.isNotBlank() }
                    ?: "کاربر",
            mobile = mobile,
            nationalCode = nationalCode,
            avatarUrl = avatarUrl
                    ?: resolvedAvatarUrl,
            email = email)
    }

    fun ProfileWrapperDto.toDomain(): PlayerProfile {
        val userInfo = user?.toUserInfo()
                ?: UserInfo(
                    0,
                    "کاربر",
                    null,
                    null,
                    null,
                    null
                )
        val playerDomain = player?.toDomain()
        val guardiansDomain = guardians.mapNotNull { it.toDomain() }
        return PlayerProfile(
            user = userInfo,
            player = playerDomain,
            guardians = guardiansDomain
        )
    }

    fun EvaluationDto.toDomain(): Evaluation? {
        val eid = id
                ?: return null
        return Evaluation(
            id = eid,
            playerId = playerId
                    ?: 0,
            sessionId = sessionId,
            classId = classId,
            classTitle = classTitle,
            coachName = coachName,
            sessionDate = sessionDate,
            startTime = startTime,
            evaluationType = evaluationType,
            technicalScore = technicalScore,
            disciplineScore = disciplineScore,
            physicalScore = physicalScore,
            teamworkScore = teamworkScore,
            overallScore = overallScore,
            strengths = strengths,
            weaknesses = weaknesses,
            notes = notes,
            createdAt = createdAt
        )
    }
}
