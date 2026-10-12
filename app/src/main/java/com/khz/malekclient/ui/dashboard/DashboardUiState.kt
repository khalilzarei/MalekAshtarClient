package com.khz.malekclient.ui.dashboard

import com.khz.malekclient.domain.model.MyChild
import com.khz.malekclient.domain.model.MyFinance
import com.khz.malekclient.domain.model.MyScheduleItem
import com.khz.malekclient.domain.model.NewsItem
import com.khz.malekclient.domain.model.User

/**
 * UI state داشبورد — همه‌ی اطلاعات ۴ منبع (me/children, me/schedule, me/news, chat/rooms)
 */
data class DashboardUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val child: MyChild? = null,
    val finance: MyFinance? = null,
    val upcoming: List<MyScheduleItem> = emptyList(),
    val recentNews: List<NewsItem> = emptyList(),
    val unreadChats: Int = 0,
    val error: String? = null,
    /** اگر داده از کش آفلاین آمده، بنر کوچک نشان داده می‌شود */
    val isFromCache: Boolean = false,

    // یادآوری بدهی — فقط یک‌بار در هر نشست (نشان نمی‌شود هر بار که به داشبورد برگشتیم)
    val showDebtDialog: Boolean = false,
    val debtDialogShown: Boolean = false
)
