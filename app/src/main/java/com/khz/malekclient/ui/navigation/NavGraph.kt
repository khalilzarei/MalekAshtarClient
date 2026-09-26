package com.khz.malekclient.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.khz.malekclient.core.notifications.ChatDeepLink
import com.khz.malekclient.ui.auth.ChangePasswordScreen
import com.khz.malekclient.ui.auth.LoginScreen
import com.khz.malekclient.ui.chat.ChatContactsScreen
import com.khz.malekclient.ui.chat.ChatRoomListScreen
import com.khz.malekclient.ui.chat.ChatScreen
import com.khz.malekclient.ui.classes.ClassesScreen
import com.khz.malekclient.ui.dashboard.DashboardScreen
import com.khz.malekclient.ui.finance.FinanceScreen
import com.khz.malekclient.ui.matches.MatchesScreen
import com.khz.malekclient.ui.news.NewsDetailScreen
import com.khz.malekclient.ui.news.NewsListScreen
import com.khz.malekclient.ui.profile.ProfileScreen
import com.khz.malekclient.ui.splash.SplashScreen
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.khz.malekclient.MalekClientApp
import com.khz.malekclient.ui.components.NetworkStatusBanner

/**
 * NavGraph اصلی اپ
 *
 * شروع از Splash:
 *  - اگر token معتبر + role=player بود → Dashboard
 *  - در غیر این صورت → Login
 *
 * must_change_password → ChangePassword (اجباری قبل از Dashboard)
 */
@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val container = (context.applicationContext as MalekClientApp).container
    val scope = rememberCoroutineScope()

    // Box دور NavHost برای نمایش بنر وضعیت شبکه (اینترنت/فیلترشکن) روی همه‌ی صفحات
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route
        ) {

            // ─── Splash ───
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        // deep-link نوتیفیکیشن: اگر اتاق خاصی درخواست شده، مستقیم به همان چت برو
                        val pendingRoom = ChatDeepLink.pendingRoomId
                        if (pendingRoom > 0) {
                            ChatDeepLink.pendingRoomId = 0
                            navController.navigate(Screen.chatWithRoom(pendingRoom)) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToChangePassword = {
                        navController.navigate(Screen.ChangePassword.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    })
            }

            // ─── Login ───
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { user ->
                        if (user.mustChangePassword) {
                            navController.navigate(Screen.ChangePassword.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            // deep-link نوتیفیکیشن بعد از لاگین
                            val pendingRoom = ChatDeepLink.pendingRoomId
                            if (pendingRoom > 0) {
                                ChatDeepLink.pendingRoomId = 0
                                navController.navigate(Screen.chatWithRoom(pendingRoom)) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    onRoleWrong = { /* پیام در خود صفحه نشان داده شد */ },
                    onLogoutFromWrongRole = {
                        scope.launch {
                            container.authRepository.logout()
                        }
                    })
            }

            // ─── ChangePassword ───
            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    onSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.ChangePassword.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() })
            }

            // ─── Dashboard ───
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToChats = { navController.navigate(Screen.ChatRoomList.route) },
                    onNavigateToNews = { navController.navigate(Screen.NewsList.route) },
                    onNavigateToNewsDetail = { newsId ->
                        navController.navigate(Screen.NewsDetail.create(newsId))
                    },
                    onNavigateToClasses = { navController.navigate(Screen.Classes.route) },
                    onNavigateToFinance = { navController.navigate(Screen.Finance.route) },
                    onNavigateToMatches = { navController.navigate(Screen.Matches.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onLogout = {
                        scope.launch {
                            container.authRepository.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    })
            }

            // ─── News ───
            composable(Screen.NewsList.route) {
                NewsListScreen(
                    onBack = { navController.popBackStack() },
                    onNewsClick = { newsId ->
                        navController.navigate(Screen.NewsDetail.create(newsId))
                    })
            }

            composable(
                route = Screen.NewsDetail.route,
                arguments = listOf(navArgument("newsId") { type = NavType.IntType })
            ) { entry ->
                val newsId = entry.arguments?.getInt("newsId")
                        ?: -1
                NewsDetailScreen(
                    newsId = newsId,
                    onBack = { navController.popBackStack() })
            }

            // ─── Classes ───
            composable(Screen.Classes.route) {
                ClassesScreen(
                    onBack = { navController.popBackStack() },
                    onOpenChat = { userId, roomId ->
                        navController.navigate(
                            Screen.chatWithRoom(roomId)
                        )
                    })
            }

            // ─── Finance ───
            composable(Screen.Finance.route) {
                FinanceScreen(onBack = { navController.popBackStack() })
            }

            // ─── Matches ───
            composable(Screen.Matches.route) {
                MatchesScreen(onBack = { navController.popBackStack() })
            }

            // ─── Profile ───
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() })
            }

            // ─── Chat: Room List ───
            composable(Screen.ChatRoomList.route) {
                ChatRoomListScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onOpenChat = { roomId ->
                        navController.navigate(
                            Screen.chatWithRoom(roomId)
                        )
                    },
                    onOpenContacts = {
                        navController.navigate(Screen.ChatContacts.route)
                    })
            }

            // ─── Chat: Contacts ───
            composable(Screen.ChatContacts.route) {
                ChatContactsScreen(
                    onBack = { navController.popBackStack() },
                    onSelect = { coachUserId -> navController.navigate(Screen.chatWithUser(coachUserId)) })
            }

            // ─── Chat (پارامتریک) — مقصد deep-link نوتیفیکیشن ───
            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("roomId") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("userId") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" })
            ) { entry ->
                val roomId = entry.arguments?.getInt("roomId")
                    ?.takeIf { it > 0 }
                val userId = entry.arguments?.getInt("userId")
                    ?.takeIf { it > 0 }
                val title = entry.arguments?.getString("title")
                    ?.takeIf { it.isNotBlank() }

                ChatScreen(
                    onBack = { navController.popBackStack() },
                    roomId = roomId,
                    targetUserId = userId,
                    initialTitle = title
                )
            }
        }

        // بنر وضعیت شبکه: اینترنت قطع / فیلترشکن روشن (روی همه‌ی صفحات)
        NetworkStatusBanner(
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}