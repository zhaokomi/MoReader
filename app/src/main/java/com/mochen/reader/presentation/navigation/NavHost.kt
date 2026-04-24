package com.mochen.reader.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mochen.reader.R
import com.mochen.reader.presentation.bookshelf.BookshelfScreen
import com.mochen.reader.presentation.detail.BookDetailScreen
import com.mochen.reader.presentation.reader.ReaderScreen
import com.mochen.reader.presentation.reader.SearchScreen
import com.mochen.reader.presentation.settings.SettingsScreen
import com.mochen.reader.presentation.settings.GroupManagementScreen
import com.mochen.reader.presentation.settings.WifiTransferScreen
import com.mochen.reader.presentation.statistics.StatisticsScreen

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Bookshelf.route,
        title = "书架",
        selectedIcon = { Icon(Icons.Filled.LibraryBooks, contentDescription = null) },
        unselectedIcon = { Icon(Icons.Outlined.LibraryBooks, contentDescription = null) }
    ),
    BottomNavItem(
        route = Screen.Statistics.route,
        title = "统计",
        selectedIcon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
        unselectedIcon = { Icon(Icons.Outlined.BarChart, contentDescription = null) }
    ),
    BottomNavItem(
        route = Screen.Settings.route,
        title = "设置",
        selectedIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
        unselectedIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) }
    )
)

@Composable
fun MoReaderNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in listOf(
        Screen.Bookshelf.route,
        Screen.Statistics.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (selected) item.selectedIcon() else item.unselectedIcon()
                            },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Bookshelf.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Screen.Bookshelf.route) {
                BookshelfScreen(
                    onBookClick = { bookId ->
                        navController.navigate(Screen.BookDetail.createRoute(bookId))
                    },
                    onWifiTransferClick = {
                        navController.navigate(Screen.WifiTransfer.route)
                    },
                    onGroupManagementClick = {
                        navController.navigate(Screen.GroupManagement.route)
                    }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(
                route = Screen.BookDetail.route,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
                BookDetailScreen(
                    bookId = bookId,
                    onBackClick = { navController.popBackStack() },
                    onReadClick = { navController.navigate(Screen.Reader.createRoute(bookId)) },
                    onSearchClick = { navController.navigate(Screen.Search.createRoute(bookId)) }
                )
            }

            composable(
                route = Screen.Reader.route,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
                ReaderScreen(
                    bookId = bookId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Search.route,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
                SearchScreen(
                    bookId = bookId,
                    onBackClick = { navController.popBackStack() },
                    onResultClick = { chapterIndex ->
                        navController.popBackStack()
                        navController.navigate(Screen.Reader.createRoute(bookId))
                    }
                )
            }

            composable(Screen.WifiTransfer.route) {
                WifiTransferScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.GroupManagement.route) {
                GroupManagementScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
