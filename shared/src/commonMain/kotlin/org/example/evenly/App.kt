package org.example.evenly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import org.example.evenly.model.ExpenseId
import org.example.evenly.model.GroupId
import org.example.evenly.security.BiometricAuthenticator
import org.example.evenly.ui.creategroup.CreateGroupScreen
import org.example.evenly.ui.creategroup.CreateGroupViewModel
import org.example.evenly.ui.expenseeditor.ExpenseEditorScreen
import org.example.evenly.ui.expenseeditor.ExpenseEditorViewModel
import org.example.evenly.ui.group.GroupScreen
import org.example.evenly.ui.group.GroupViewModel
import org.example.evenly.ui.groups.GroupsScreen
import org.example.evenly.ui.groups.GroupsViewModel
import org.example.evenly.ui.lock.AppLockGate
import org.example.evenly.ui.lock.AppLockViewModel
import org.example.evenly.ui.navigation.CreateGroupRoute
import org.example.evenly.ui.navigation.ExpenseEditorRoute
import org.example.evenly.ui.navigation.GroupRoute
import org.example.evenly.ui.navigation.GroupsRoute
import org.example.evenly.ui.navigation.SettingsRoute
import org.example.evenly.ui.settings.SettingsScreen
import org.example.evenly.ui.settings.SettingsViewModel
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.EvenlyTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val FOREGROUND_SYNC_INTERVAL: Duration = 30.seconds

@Composable
fun App(biometricAuthenticator: BiometricAuthenticator, graph: AppGraph) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                graph.syncCoordinator.requestSync()
                delay(FOREGROUND_SYNC_INTERVAL)
            }
        }
    }

    EvenlyTheme {
        AppLockGate(biometricAuthenticator = biometricAuthenticator, viewModel = viewModel { AppLockViewModel(graph.settingsRepository) }) {
            val navController = rememberNavController()

            NavHost(modifier = Modifier.fillMaxSize().background(AppColors.surfaceBase), navController = navController, startDestination = GroupsRoute) {
                composable<GroupsRoute> {
                    GroupsScreen(
                        onCreateGroup = { navController.navigate(CreateGroupRoute) },
                        onOpenGroup = { groupId -> navController.navigate(GroupRoute(groupId = groupId.raw)) },
                        onOpenSettings = { navController.navigate(SettingsRoute) },
                        viewModel = viewModel { GroupsViewModel(groupRepository = graph.groupRepository, syncCoordinator = graph.syncCoordinator) },
                    )
                }
                composable<CreateGroupRoute> {
                    CreateGroupScreen(
                        onBack = { navController.popBackStack() },
                        onGroupCreated = { groupId -> navController.navigate(GroupRoute(groupId = groupId.raw)) { popUpTo<CreateGroupRoute> { inclusive = true } } },
                        viewModel = viewModel { CreateGroupViewModel(graph.groupRepository) },
                    )
                }
                composable<GroupRoute> { entry ->
                    val route = entry.toRoute<GroupRoute>()

                    GroupScreen(
                        onAddExpense = { navController.navigate(ExpenseEditorRoute(groupId = route.groupId)) },
                        onBack = { navController.popBackStack() },
                        onEditExpense = { expenseId -> navController.navigate(ExpenseEditorRoute(groupId = route.groupId, expenseId = expenseId.raw)) },
                        onGroupDeleted = { navController.popBackStack(inclusive = false, route = GroupsRoute) },
                        viewModel = viewModel {
                            GroupViewModel(
                                expenseRepository = graph.expenseRepository,
                                groupId = GroupId(route.groupId),
                                groupRepository = graph.groupRepository,
                                settlementRepository = graph.settlementRepository,
                                syncCoordinator = graph.syncCoordinator,
                            )
                        },
                    )
                }
                composable<ExpenseEditorRoute> { entry ->
                    val route = entry.toRoute<ExpenseEditorRoute>()

                    ExpenseEditorScreen(
                        onBack = { navController.popBackStack() },
                        onFinished = { navController.popBackStack() },
                        viewModel = viewModel {
                            ExpenseEditorViewModel(
                                exchangeRateRepository = graph.exchangeRateRepository,
                                expenseId = route.expenseId?.let(::ExpenseId),
                                expenseRepository = graph.expenseRepository,
                                groupId = GroupId(route.groupId),
                                groupRepository = graph.groupRepository,
                            )
                        },
                    )
                }
                composable<SettingsRoute> {
                    SettingsScreen(
                        biometricAuthenticator = biometricAuthenticator,
                        onBack = { navController.popBackStack() },
                        viewModel = viewModel { SettingsViewModel(settingsRepository = graph.settingsRepository, syncCoordinator = graph.syncCoordinator) },
                    )
                }
            }
        }
    }
}
