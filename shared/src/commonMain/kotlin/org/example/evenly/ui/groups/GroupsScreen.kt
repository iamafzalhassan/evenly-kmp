package org.example.evenly.ui.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.action_join
import evenly.shared.generated.resources.action_join_group
import evenly.shared.generated.resources.action_new_group
import evenly.shared.generated.resources.action_open_settings
import evenly.shared.generated.resources.field_invite_code
import evenly.shared.generated.resources.group_tile_subtitle
import evenly.shared.generated.resources.groups_empty_message
import evenly.shared.generated.resources.groups_empty_title
import evenly.shared.generated.resources.groups_title
import evenly.shared.generated.resources.join_failed
import evenly.shared.generated.resources.join_group_title
import evenly.shared.generated.resources.join_not_found
import evenly.shared.generated.resources.join_offline
import evenly.shared.generated.resources.join_rate_limited
import evenly.shared.generated.resources.member_count
import evenly.shared.generated.resources.sync_failed
import org.example.evenly.model.Group
import org.example.evenly.model.GroupId
import org.example.evenly.ui.components.AppListTile
import org.example.evenly.ui.components.AppPullToRefreshBox
import org.example.evenly.ui.components.AppSnackbarHost
import org.example.evenly.ui.components.AppTopBar
import org.example.evenly.ui.components.PrimaryButton
import org.example.evenly.ui.components.ScreenEmptyState
import org.example.evenly.ui.components.SecondaryButton
import org.example.evenly.ui.components.TextInputSheet
import org.example.evenly.ui.components.rememberAppSnackbarState
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GroupsScreen(onCreateGroup: () -> Unit, onOpenSettings: () -> Unit, onOpenGroup: (GroupId) -> Unit, viewModel: GroupsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = rememberAppSnackbarState()
    var isJoining by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.joinedGroupId) {
        val groupId = state.joinedGroupId ?: return@LaunchedEffect
        viewModel.onEvent(GroupsEvent.ConsumeJoinedGroup)
        onOpenGroup(groupId)
    }

    LaunchedEffect(state.feedback) {
        val message = when (state.feedback) {
            GroupsFeedback.INVITE_NOT_FOUND -> Res.string.join_not_found
            GroupsFeedback.JOIN_FAILED -> Res.string.join_failed
            GroupsFeedback.JOIN_OFFLINE -> Res.string.join_offline
            GroupsFeedback.JOIN_RATE_LIMITED -> Res.string.join_rate_limited
            GroupsFeedback.SYNC_FAILED -> Res.string.sync_failed
            null -> return@LaunchedEffect
        }
        snackbarState.showError(getString(message))
        viewModel.onEvent(GroupsEvent.DismissFeedback)
    }

    Box(modifier = modifier.fillMaxSize().background(AppColors.surfaceBase)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(Res.string.groups_title)) {
                IconButton(onClick = { isJoining = true }) {
                    Icon(contentDescription = stringResource(Res.string.action_join_group), imageVector = Icons.Outlined.GroupAdd, tint = AppColors.primary)
                }
                IconButton(onClick = onCreateGroup) {
                    Icon(contentDescription = stringResource(Res.string.action_new_group), imageVector = Icons.Outlined.Add, tint = AppColors.primary)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(contentDescription = stringResource(Res.string.action_open_settings), imageVector = Icons.Outlined.Settings, tint = AppColors.primary)
                }
            }
            AppPullToRefreshBox(modifier = Modifier.weight(1f).fillMaxWidth(), isRefreshing = state.isRefreshing || state.isJoining, onRefresh = { viewModel.onEvent(GroupsEvent.Refresh) }) {
                when {
                    state.isLoading -> Unit
                    state.groups.isEmpty() -> ScreenEmptyState(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Outlined.Groups,
                        message = stringResource(Res.string.groups_empty_message),
                        title = stringResource(Res.string.groups_empty_title),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                            PrimaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(Res.string.action_new_group), onClick = onCreateGroup)
                            SecondaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(Res.string.action_join_group), onClick = { isJoining = true })
                        }
                    }
                    else -> GroupList(groups = state.groups, onOpenGroup = onOpenGroup)
                }
            }
        }
        AppSnackbarHost(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(), state = snackbarState)
    }

    if (isJoining) {
        TextInputSheet(
            capitalization = KeyboardCapitalization.Characters,
            confirmLabel = stringResource(Res.string.action_join),
            errorMessage = { null },
            initialValue = "",
            label = stringResource(Res.string.field_invite_code),
            onConfirm = { code ->
                isJoining = false
                viewModel.onEvent(GroupsEvent.JoinGroup(code))
            },
            onDismiss = { isJoining = false },
            title = stringResource(Res.string.join_group_title),
        )
    }
}

@Composable
private fun GroupList(groups: List<Group>, onOpenGroup: (GroupId) -> Unit, modifier: Modifier = Modifier) {
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl + navigationBarBottom, end = AppSpacing.screenPadding, start = AppSpacing.screenPadding, top = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        items(items = groups, key = { group -> group.id.raw }) { group ->
            val memberCount = pluralStringResource(Res.plurals.member_count, group.members.size, group.members.size)

            AppListTile(
                icon = Icons.Outlined.Groups,
                onClick = { onOpenGroup(group.id) },
                subtitle = stringResource(Res.string.group_tile_subtitle, memberCount, group.currency.name),
                title = group.name,
            )
        }
    }
}
