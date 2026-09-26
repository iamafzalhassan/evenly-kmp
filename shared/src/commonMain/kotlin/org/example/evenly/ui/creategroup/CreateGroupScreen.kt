package org.example.evenly.ui.creategroup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.action_add_member
import evenly.shared.generated.resources.action_create_group
import evenly.shared.generated.resources.action_remove_member
import evenly.shared.generated.resources.create_group_currency_heading
import evenly.shared.generated.resources.create_group_currency_hint
import evenly.shared.generated.resources.create_group_details_heading
import evenly.shared.generated.resources.create_group_members_heading
import evenly.shared.generated.resources.create_group_members_hint
import evenly.shared.generated.resources.create_group_save_failed
import evenly.shared.generated.resources.create_group_title
import evenly.shared.generated.resources.error_duplicate_members
import evenly.shared.generated.resources.field_currency
import evenly.shared.generated.resources.field_group_name
import evenly.shared.generated.resources.field_member_numbered
import org.example.evenly.model.GroupId
import org.example.evenly.ui.components.AppPickerField
import org.example.evenly.ui.components.AppSnackbarHost
import org.example.evenly.ui.components.AppTextField
import org.example.evenly.ui.components.AppTopBar
import org.example.evenly.ui.components.CurrencyPickerSheet
import org.example.evenly.ui.components.PrimaryButton
import org.example.evenly.ui.components.SecondaryButton
import org.example.evenly.ui.components.SectionHeader
import org.example.evenly.ui.components.currencyLabel
import org.example.evenly.ui.components.rememberAppSnackbarState
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateGroupScreen(onBack: () -> Unit, onGroupCreated: (GroupId) -> Unit, viewModel: CreateGroupViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = rememberAppSnackbarState()
    var isPickingCurrency by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.createdGroupId) {
        state.createdGroupId?.let(onGroupCreated)
    }

    LaunchedEffect(state.isSaveFailed) {
        if (state.isSaveFailed) {
            snackbarState.showError(getString(Res.string.create_group_save_failed))
            viewModel.onEvent(CreateGroupEvent.DismissSaveFailure)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(AppColors.surfaceBase)) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            AppTopBar(onBack = onBack, title = stringResource(Res.string.create_group_title))
            CreateGroupForm(modifier = Modifier.weight(1f), onEvent = viewModel::onEvent, onPickCurrency = { isPickingCurrency = true }, state = state)
        }
        AppSnackbarHost(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(), state = snackbarState)
    }

    if (isPickingCurrency) {
        CurrencyPickerSheet(
            onDismiss = { isPickingCurrency = false },
            onSelect = { currency ->
                isPickingCurrency = false
                viewModel.onEvent(CreateGroupEvent.ChangeCurrency(currency))
            },
            selected = state.currency,
            title = stringResource(Res.string.create_group_currency_heading),
        )
    }
}

@Composable
private fun CreateGroupForm(onPickCurrency: () -> Unit, onEvent: (CreateGroupEvent) -> Unit, state: CreateGroupUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = AppSpacing.xl, end = AppSpacing.screenPadding, start = AppSpacing.screenPadding, top = AppSpacing.lg)) {
        SectionHeader(label = stringResource(Res.string.create_group_details_heading))
        AppTextField(capitalization = KeyboardCapitalization.Words, label = stringResource(Res.string.field_group_name), onValueChange = { onEvent(CreateGroupEvent.ChangeName(it)) }, value = state.name)
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        SectionHeader(label = stringResource(Res.string.create_group_currency_heading))
        AppPickerField(label = stringResource(Res.string.field_currency), onClick = onPickCurrency, value = currencyLabel(state.currency))
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(style = AppTheme.textStyles.listSecondary, text = stringResource(Res.string.create_group_currency_hint))
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        SectionHeader(label = stringResource(Res.string.create_group_members_heading))
        Text(style = AppTheme.textStyles.listSecondary, text = stringResource(Res.string.create_group_members_hint))
        Spacer(modifier = Modifier.height(AppSpacing.md))
        state.memberNames.forEachIndexed { index, memberName ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
            MemberNameRow(
                canRemove = state.canRemoveMember,
                index = index,
                onRemove = { onEvent(CreateGroupEvent.RemoveMember(index)) },
                onValueChange = { onEvent(CreateGroupEvent.ChangeMemberName(index = index, name = it)) },
                value = memberName,
            )
        }
        if (state.hasDuplicateMembers) {
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.errorHint, text = stringResource(Res.string.error_duplicate_members))
        }
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        SecondaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(Res.string.action_add_member), onClick = { onEvent(CreateGroupEvent.AddMember) })
        Spacer(modifier = Modifier.height(AppSpacing.md))
        PrimaryButton(modifier = Modifier.fillMaxWidth(), isEnabled = state.canCreate, label = stringResource(Res.string.action_create_group), onClick = { onEvent(CreateGroupEvent.Create) })
    }
}

@Composable
private fun MemberNameRow(canRemove: Boolean, index: Int, value: String, onRemove: () -> Unit, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
        AppTextField(
            modifier = Modifier.weight(1f),
            capitalization = KeyboardCapitalization.Words,
            label = stringResource(Res.string.field_member_numbered, index + 1),
            onValueChange = onValueChange,
            value = value,
        )
        IconButton(enabled = canRemove, onClick = onRemove) {
            Icon(contentDescription = stringResource(Res.string.action_remove_member), imageVector = Icons.Outlined.Delete, tint = if (canRemove) AppColors.primary else AppColors.textDisabled)
        }
    }
}
