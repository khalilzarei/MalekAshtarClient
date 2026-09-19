package com.khz.malekashtarclient.data.mapper

import com.khz.malekashtarclient.data.dto.response.ChatContactDto
import com.khz.malekashtarclient.data.dto.response.ChatMessageDto
import com.khz.malekashtarclient.data.dto.response.ChatMessageSenderDto
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
 */
object ClientMapper {

    /* ═══════ me/children ═══════ */

    fun MyChildDto.toDomain(): MyChild? {
        val pid = id ?: return null
        return MyChild(
            id = pid,
            fullName = fullName?.takeIf { it.isNotBlank() } ?: "بازیکن #$pid",
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
        val cid = id ?: return null
        return MyChildClass(
            id = cid,
            title = title?.takeIf { it.isNotBlank() } ?: "کلاس #$cid",
            capacity = capacity,
            enrolledCount = enrolledCount,
            status = status
        )
    }

    fun MyChildBalanceDto.toDomain(): MyChildBalance = MyChildBalance(
        playerId = playerId ?: 0,
        debt = debt ?: 0L,
        totalPaid = totalPaid ?: 0L,
        pendingPaymentsCount = pendingPaymentsCount ?: 0
    )

    /* ═══════ me/schedule ═══════ */

    fun MyScheduleDto.toDomain(): MyScheduleItem? {
        val sid = id ?: return null
        return MyScheduleItem(
            id = sid,
            classId = classId ?: 0,
            classTitle = classTitle?.takeIf { it.isNotBlank() } ?: "کلاس",
            sessionDate = sessionDate ?: "",
            startTime = startTime ?: "",
            endTime = endTime,
            location = location,
            topic = topic,
            status = status,
            notes = notes
        )
    }

    /* ═══════ me/news ═══════ */

    fun NewsDto.toDomain(): NewsItem? {
        val nid = id ?: return null
        return NewsItem(
            id = nid,
            title = title?.takeIf { it.isNotBlank() } ?: "(بدون عنوان)",
            body = body,
            publishedAt = publishedAt,
            createdAt = createdAt
        )
    }

    /* ═══════ me/classes ═══════ */

    fun MyClassDto.toDomain(): MyClass? {
        val cid = id ?: return null
        return MyClass(
            id = cid,
            title = title?.takeIf { it.isNotBlank() } ?: "کلاس #$cid",
            ageGroupTitle = ageGroupTitle,
            coachName = coachName,
            coachId = coachId,
            assistantCoachName = assistantCoachName,
            location = location,
            description = description,
            capacity = capacity,
            enrolledCount = enrolledCount,
            schedules = schedules.mapNotNull { it.toDomain() }
        )
    }

    fun ClassScheduleItemDto.toDomain(): MyClassSchedule? {
        val wd = weekday ?: return null
        return MyClassSchedule(
            weekday = wd,
            weekdayLabel = weekdayLabel?.takeIf { it.isNotBlank() } ?: "",
            startTime = startTime ?: "",
            endTime = endTime ?: "",
            location = location
        )
    }

    /* ═══════ me/finance ═══════ */

    fun MyFinanceDto.toDomain(): MyFinance? {
        val pid = playerId ?: return null
        return MyFinance(
            playerId = pid,
            playerName = playerName?.takeIf { it.isNotBlank() } ?: "بازیکن #$pid",
            totalInvoiced = totalInvoiced ?: 0L,
            totalPaid = totalPaid ?: 0L,
            balance = balance ?: 0L,
            pendingPayments = pendingPayments ?: 0,
            pendingAmount = pendingAmount ?: 0L,
            invoices = invoices.mapNotNull { it.toDomain() }
        )
    }

    fun MyInvoiceDto.toDomain(): MyInvoice? {
        val iid = id ?: return null
        return MyInvoice(
            id = iid,
            invoiceNumber = invoiceNumber,
            invoiceType = invoiceType,
            periodStartDate = periodStartDate,
            periodEndDate = periodEndDate,
            dueDate = dueDate,
            status = status,
            totalAmount = totalAmount ?: 0L,
            paidAmount = paidAmount ?: 0L,
            remainingAmount = remainingAmount ?: 0L
        )
    }

    /* ═══════ me/matches ═══════ */

    fun MyMatchDto.toDomain(): MyMatch? {
        val mid = id ?: return null
        return MyMatch(
            id = mid,
            title = title,
            matchType = matchType,
            opponentTeam = opponentTeam,
            matchDate = matchDate ?: "",
            matchTime = matchTime ?: "",
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
        val uid = userId ?: return null
        return ChatContact(
            userId = uid,
            fullName = fullName?.takeIf { it.isNotBlank() } ?: "کاربر #$uid",
            role = role ?: "admin",
            avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
            classTitle = classTitle
        )
    }

    fun ChatRoomDto.toDomain(): ChatRoom? {
        val rid = id ?: return null
        return ChatRoom(
            id = rid,
            roomType = roomType?.takeIf { it.isNotBlank() } ?: "player_admin",
            targetUserId = targetUserId,
            targetUserName = targetUserName,
            targetUserRole = targetUserRole,
            targetUserAvatar = targetUserAvatar?.takeIf { it.isNotBlank() },
            playerId = playerId,
            classId = classId,
            classTitle = classTitle,
            ageGroupId = ageGroupId,
            ageGroupTitle = ageGroupTitle,
            isLocked = isLocked == true,
            memberCount = memberCount ?: 0,
            subject = subject,
            lastMessage = lastMessage,
            lastMessageAt = lastMessageAt,
            unreadCount = unreadCount ?: 0,
            members = members.mapNotNull { it.toDomain() }
        )
    }

    fun ChatRoomMemberDto.toDomain(): ChatRoomMember? {
        val uid = userId ?: return null
        return ChatRoomMember(
            userId = uid,
            fullName = fullName?.takeIf { it.isNotBlank() } ?: "عضو #$uid",
            role = role,
            avatarUrl = avatarUrl?.takeIf { it.isNotBlank() }
        )
    }

    fun ChatMessageDto.toDomain(): ChatMessage? {
        val mid = id ?: return null
        return ChatMessage(
            id = mid,
            roomId = roomId ?: 0,
            senderId = senderId,
            senderName = sender?.fullName,
            senderRole = sender?.role,
            senderAvatar = sender?.avatarUrl?.takeIf { it.isNotBlank() },
            messageType = messageType ?: "text",
            body = body ?: "",
            isRead = isRead == true,
            createdAt = createdAt
        )
    }
}
