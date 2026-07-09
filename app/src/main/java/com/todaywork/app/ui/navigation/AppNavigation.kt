package com.todaywork.app.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.todaywork.app.ui.screens.calendar.CalendarScreen
import com.todaywork.app.ui.screens.calendar.CalendarViewModel
import com.todaywork.app.ui.screens.salary.SalaryScreen
import com.todaywork.app.ui.screens.schedule.ScheduleScreen
import com.todaywork.app.ui.screens.settings.SettingsScreen

sealed class NavRoute(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    data object Calendar : NavRoute("calendar", "달력",
        Icons.Default.CalendarMonth, Icons.Default.CalendarMonth)
    data object Schedule : NavRoute("schedule", "근무표",
        Icons.Default.AccessTime, Icons.Default.AccessTime)
    data object Salary   : NavRoute("salary", "급여",
        Icons.Default.AccountBalanceWallet, Icons.Default.AccountBalanceWallet)
    data object Settings : NavRoute("settings", "설정",
        Icons.Default.Settings, Icons.Default.Settings)
}

private val bottomNavItems = listOf(
    NavRoute.Calendar,
    NavRoute.Schedule,
    NavRoute.Salary,
    NavRoute.Settings
)

@Composable
fun AppNavigation(windowSizeClass: WindowSizeClass, initialDateStr: String? = null) {
    val navController = rememberNavController()
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("마님 오늘도 수고하셨습니다~♡") },
            text = { Text("앱을 종료하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = { (context as? Activity)?.finish() }
                ) {
                    Text("종료")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (isCompact) {
        // ── 폰: BottomNavigation ──────────────────────────────
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                initialDateStr = initialDateStr,
                modifier = Modifier.padding(innerPadding)
            )
        }
    } else {
        // ── 태블릿/대화면: NavigationRail ────────────────────
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        androidx.compose.foundation.layout.Row {
            NavigationRail {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == item.route } == true
                    NavigationRailItem(
                        icon = {
                            Icon(
                                if (selected) item.selectedIcon else item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
            AppNavHost(navController = navController, initialDateStr = initialDateStr, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AppNavHost(
    navController: androidx.navigation.NavHostController,
    initialDateStr: String? = null,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Calendar.route,
        modifier = modifier
    ) {
        composable(NavRoute.Calendar.route) { 
            val viewModel: CalendarViewModel = hiltViewModel()
            LaunchedEffect(initialDateStr) {
                if (initialDateStr != null) {
                    val date = runCatching { java.time.LocalDate.parse(initialDateStr) }.getOrNull()
                    if (date != null) {
                        viewModel.goToDate(date)
                    }
                }
            }
            CalendarScreen(viewModel) 
        }
        composable(NavRoute.Schedule.route) { ScheduleScreen() }
        composable(NavRoute.Salary.route)   { SalaryScreen()   }
        composable(NavRoute.Settings.route) { SettingsScreen() }
    }
}
