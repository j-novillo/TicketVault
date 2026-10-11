package com.ticketvault.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ticketvault.R
import com.ticketvault.ui.capture.CaptureScreen
import com.ticketvault.ui.metrics.MetricsScreen
import com.ticketvault.ui.review.TicketReviewScreen
import com.ticketvault.ui.review.TicketReviewViewModel
import com.ticketvault.ui.settings.SettingsScreen
import com.ticketvault.ui.tickets.TicketsScreen

private enum class TopLevelDestination(
    val route: String,
    val labelRes: Int,
) {
    Capture("capture", R.string.nav_capture),
    Tickets("tickets", R.string.nav_tickets),
    Metrics("metrics", R.string.nav_metrics),
    Settings("settings", R.string.nav_settings),
}

private const val REVIEW_ROUTE = "review/{${TicketReviewViewModel.TICKET_ID_ARG}}"
private const val TICKET_SAVED_KEY = "ticket_saved"

@Composable
fun TicketVaultApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isReviewing = currentDestination?.route == REVIEW_ROUTE

    Scaffold(
        bottomBar = {
            if (!isReviewing) TopLevelNavigationBar(navController, currentDestination)
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Capture.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable(TopLevelDestination.Capture.route) { entry ->
                val ticketSaved by entry.savedStateHandle
                    .getStateFlow(TICKET_SAVED_KEY, false)
                    .collectAsStateWithLifecycle()
                CaptureScreen(
                    onTicketRecognized = { ticketId -> navController.navigate("review/$ticketId") },
                    showTicketSavedMessage = ticketSaved,
                    onTicketSavedMessageShown = { entry.savedStateHandle[TICKET_SAVED_KEY] = false },
                )
            }
            composable(
                route = REVIEW_ROUTE,
                arguments = listOf(navArgument(TicketReviewViewModel.TICKET_ID_ARG) { type = NavType.LongType }),
            ) {
                TicketReviewScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.previousBackStackEntry?.savedStateHandle?.set(TICKET_SAVED_KEY, true)
                        navController.popBackStack()
                    },
                )
            }
            composable(TopLevelDestination.Tickets.route) { TicketsScreen() }
            composable(TopLevelDestination.Metrics.route) { MetricsScreen() }
            composable(TopLevelDestination.Settings.route) { SettingsScreen() }
        }
    }
}

@Composable
private fun TopLevelNavigationBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (destination) {
                            TopLevelDestination.Capture -> Icons.Default.CameraAlt
                            TopLevelDestination.Tickets -> Icons.Default.Receipt
                            TopLevelDestination.Metrics -> Icons.Default.BarChart
                            TopLevelDestination.Settings -> Icons.Default.Settings
                        },
                        contentDescription = stringResource(destination.labelRes),
                    )
                },
                label = { Text(stringResource(destination.labelRes)) },
            )
        }
    }
}
