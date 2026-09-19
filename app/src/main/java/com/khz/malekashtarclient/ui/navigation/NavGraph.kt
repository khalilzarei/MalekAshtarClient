package com.khz.malekashtarclient.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.khz.malekashtarclient.core.local.SessionManager
import com.khz.malekashtarclient.core.util.Constants
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.ui.auth.ChangePasswordScreen
import com.khz.malekashtarclient.ui.auth.LoginScreen
import com.khz.malekashtarclient.ui.chat.ChatContactsScreen
import com.khz.malekashtarclient.ui.chat.ChatRoomListScreen
import com.khz.malekashtarclient.ui.chat.ChatScreen
import com.khz.malekashtarclient.ui.classes.ClassesScreen
import com.khz.malekashtarclient.ui.dashboard.DashboardScreen
import com.khz.malekashtarclient.ui.finance.FinanceScreen
import com.khz.malekashtarclient.ui.matches.MatchesScreen
import com.khz.malekashtarclient.ui.news.NewsListScreen
import com.khz.malekashtarclient.ui.profile.ProfileScreen
import com.khz.malekashtarclient.ui.splash.SplashScreen
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.khz.malekashtarclient.FootballSchoolApp

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
    val container = (context.applicationContext as FootballSchoolApp).container
    val sessionManager = container.sessionManager
    val scope = rememberCoroutineScope()

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
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToChangePassword = {
                    navController.navigate(Screen.ChangePassword.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
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
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onRoleWrong = { /* پیام در خود صفحه نشان داده شد */ },
                onLogoutFromWrongRole = {
                    scope.launch {
                        container.authRepository.logout()
                    }
                }
            )
        }

        // ─── ChangePassword ───
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                onSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.ChangePassword.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Dashboard ───
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToChats = { navController.navigate(Screen.ChatRoomList.route) },
                onNavigateToNews = { navController.navigate(Screen.NewsList.route) },
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
                }
            )
        }

        // ─── News ───
        composable(Screen.NewsList.route) {
            NewsListScreen(onBack = { navController.popBackStack() })
        }

        // ─── Classes ───
        composable(Screen.Classes.route) {
            ClassesScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { userId, roomId ->
                    navController.navigate(Screen.chatWithRoom(roomId, null))
                }
            )
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
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Chat: Room List ───
        composable(Screen.ChatRoomList.route) {
            ChatRoomListScreen(
                onBack = { navController.popBackStack() },
                onOpenRoom = { roomId, _, title, _ ->
                    navController.navigate(Screen.chatWithRoom(roomId, title))
                },
                onNewConversation = { navController.navigate(Screen.ChatContacts.route) }
            )
        }

        // ─── Chat: Contacts ───
        composable(Screen.ChatContacts.route) {
            ChatContactsScreen(
                onBack = { navController.popBackStack() },
                onSelect = { contact ->
                    navController.navigate(Screen.chatWithUser(contact.userId))
                }
            )
        }

        // ─── Chat (پارامتریک) ───
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("title") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry ->
            val roomId = entry.arguments?.getInt("roomId")?.takeIf { it > 0 }
            val userId = entry.arguments?.getInt("userId")?.takeIf { it > 0 }
            val title = entry.arguments?.getString("title")?.takeIf { it.isNotBlank() }

            ChatScreen(
                onBack = { navController.popBackStack() },
                roomId = roomId,
                targetUserId = userId,
                initialTitle = title
            )
        }
    }
}
