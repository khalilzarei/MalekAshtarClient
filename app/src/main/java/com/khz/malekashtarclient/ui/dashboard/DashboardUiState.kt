package com.khz.malekashtarclient.ui.dashboard

import com.khz.malekashtarclient.domain.model.MyChild
import com.khz.malekashtarclient.domain.model.MyScheduleItem
import com.khz.malekashtarclient.domain.model.NewsItem
import com.khz.malekashtarclient.domain.model.User

/**
 * UI state داشبورد — همه‌ی اطلاعات ۴ منبع (me/children, me/schedule, me/news, chat/rooms)
 */
data class DashboardUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val child: MyChild? = null,
    val upcoming: List<MyScheduleItem> = emptyList(),
    val recentNews: List<NewsItem> = emptyList(),
    val unreadChats: Int = 0,
    val error: String? = null
)
