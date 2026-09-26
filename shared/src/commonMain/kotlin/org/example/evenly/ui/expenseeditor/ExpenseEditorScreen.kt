package org.example.evenly.ui.expenseeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.action_back
import evenly.shared.generated.resources.action_delete_expense
import evenly.shared.generated.resources.action_done
import evenly.shared.generated.resources.action_save_expense
import evenly.shared.generated.resources.add_expense_title
import evenly.shared.generated.resources.delete_expense_failed
import evenly.shared.generated.resources.delete_expense_message
import evenly.shared.generated.resources.edit_expense_title
import evenly.shared.generated.resources.exchange_rate_converted
import evenly.shared.generated.resources.exchange_rate_hint
import evenly.shared.generated.resources.expense_details_heading
import evenly.shared.generated.resources.expense_missing_message
import evenly.shared.generated.resources.expense_missing_title
import evenly.shared.generated.resources.expense_save_failed
import evenly.shared.generated.resources.expense_split_heading
import evenly.shared.generated.resources.field_amount
import evenly.shared.generated.resources.field_currency
import evenly.shared.generated.resources.field_date
import evenly.shared.generated.resources.field_exchange_rate
import evenly.shared.generated.resources.field_expense_title
import evenly.shared.generated.resources.field_paid_by
import evenly.shared.generated.resources.field_percentage
import evenly.shared.generated.resources.member_unknown
import evenly.shared.generated.resources.save_blocker_amount
import evenly.shared.generated.resources.save_blocker_exchange_rate
import evenly.shared.generated.resources.save_blocker_payer
import evenly.shared.generated.resources.save_blocker_split
import evenly.shared.generated.resources.save_blocker_title
import evenly.shared.generated.resources.share_not_included
import evenly.shared.generated.resources.split_equal_hint
import evenly.shared.generated.resources.split_error_no_participants
import evenly.shared.generated.resources.split_exact_hint
import evenly.shared.generated.resources.split_mode_equal
import evenly.shared.generated.resources.split_mode_exact
import evenly.shared.generated.resources.split_mode_percentage
import evenly.shared.generated.resources.split_over_total
import evenly.shared.generated.resources.split_percentage_hint
import evenly.shared.generated.resources.split_remaining
import org.example.evenly.domain.ExpenseSplitter
import org.example.evenly.domain.SplitError
import org.example.evenly.model.Member
import org.example.evenly.model.Money
import org.example.evenly.ui.components.AppAmountField
import org.example.evenly.ui.components.AppDatePickerDialog
import org.example.evenly.ui.components.AppListTile
import org.example.evenly.ui.components.AppPickerField
import org.example.evenly.ui.components.AppSnackbarHost
import org.example.evenly.ui.components.AppTextField
import org.example.evenly.ui.components.AppTopBar
import org.example.evenly.ui.components.ChoiceRow
import org.example.evenly.ui.components.ConfirmSheet
import org.example.evenly.ui.components.CurrencyPickerSheet
import org.example.evenly.ui.components.PickerSheet
import org.example.evenly.ui.components.PrimaryButton
import org.example.evenly.ui.components.ScreenEmptyState
import org.example.evenly.ui.components.SectionHeader
import org.example.evenly.ui.components.currencyLabel
import org.example.evenly.ui.components.rememberAppSnackbarState
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme
import org.example.evenly.util.DateFormat
import org.example.evenly.util.LocalDates
import org.example.evenly.util.MoneyFormat
import org.example.evenly.util.PercentFormat
import org.example.evenly.util.RateFormat
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun ExpenseEditorScreen(onBack: () -> Unit, onFinished: () -> Unit, viewModel: ExpenseEditorViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = rememberAppSnackbarState()
    val unknownMember = stringResource(Res.string.member_unknown)
    val memberName: (Member) -> String = { member -> member.name.ifBlank { unknownMember } }
    var isConfirmingDelete by rememberSaveable { mutableStateOf(false) }
    var isPickingCurrency by rememberSaveable { mutableStateOf(false) }
    var isPickingDate by rememberSaveable { mutableStateOf(false) }
    var isPickingPayer by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            onFinished()
        }
    }

    LaunchedEffect(state.isSaveFailed) {
        if (state.isSaveFailed) {
            snackbarState.showError(getString(Res.string.expense_save_failed))
            viewModel.onEvent(ExpenseEditorEvent.DismissSaveFailure)
        }
    }

    LaunchedEffect(state.isDeleteFailed) {
        if (state.isDeleteFailed) {
            snackbarState.showError(getString(Res.string.delete_expense_failed))
            viewModel.onEvent(ExpenseEditorEvent.DismissDeleteFailure)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(AppColors.surfaceBase)) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            AppTopBar(onBack = onBack, title = stringResource(if (state.isEditing) Res.string.edit_expense_title else Res.string.add_expense_title)) {
                if (state.isEditing && !state.isMissing) {
                    IconButton(onClick = { isConfirmingDelete = true }) {
                        Icon(contentDescription = stringResource(Res.string.action_delete_expense), imageVector = Icons.Outlined.Delete, tint = AppColors.primary)
                    }
                }
            }
            if (state.isMissing) {
                ScreenEmptyState(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    message = stringResource(Res.string.expense_missing_message),
                    title = stringResource(Res.string.expense_missing_title),
                ) {
                    PrimaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(Res.string.action_back), onClick = onBack)
                }
            } else {
                ExpenseForm(
                    modifier = Modifier.weight(1f),
                    memberName = memberName,
                    onEvent = viewModel::onEvent,
                    onPickCurrency = { isPickingCurrency = true },
                    onPickDate = { isPickingDate = true },
                    onPickPayer = { isPickingPayer = true },
                    state = state,
                )
            }
        }
        AppSnackbarHost(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(), state = snackbarState)
    }

    if (isPickingPayer) {
        PickerSheet(
            icon = Icons.Outlined.Person,
            label = { memberName(it) },
            onDismiss = { isPickingPayer = false },
            onSelect = { member ->
                isPickingPayer = false
                viewModel.onEvent(ExpenseEditorEvent.ChangePaidBy(member.id))
            },
            options = state.members,
            selected = state.paidByMember,
            title = stringResource(Res.string.field_paid_by),
        )
    }

    if (isPickingCurrency) {
        CurrencyPickerSheet(
            onDismiss = { isPickingCurrency = false },
            onSelect = { currency ->
                isPickingCurrency = false
                viewModel.onEvent(ExpenseEditorEvent.ChangeCurrency(currency))
            },
            selected = state.expenseCurrency,
            title = stringResource(Res.string.field_currency),
        )
    }

    if (isPickingDate) {
        AppDatePickerDialog(
            confirmLabel = stringResource(Res.string.action_done),
            initialUtcDateMillis = LocalDates.toUtcDateMillis(state.spentAt ?: Clock.System.now()),
            maxUtcDateMillis = LocalDates.todayUtcDateMillis(),
            onConfirm = { utcDateMillis ->
                isPickingDate = false
                viewModel.onEvent(ExpenseEditorEvent.ChangeDate(utcDateMillis))
            },
            onDismiss = { isPickingDate = false },
        )
    }

    if (isConfirmingDelete) {
        ConfirmSheet(
            confirmLabel = stringResource(Res.string.action_delete_expense),
            isDestructive = true,
            message = stringResource(Res.string.delete_expense_message, state.title.trim()),
            onConfirm = {
                isConfirmingDelete = false
                viewModel.onEvent(ExpenseEditorEvent.Delete)
            },
            onDismiss = { isConfirmingDelete = false },
            title = stringResource(Res.string.action_delete_expense),
        )
    }
}

@Composable
private fun ExpenseForm(
    onPickCurrency: () -> Unit,
    onPickDate: () -> Unit,
    onPickPayer: () -> Unit,
    onEvent: (ExpenseEditorEvent) -> Unit,
    memberName: (Member) -> String,
    state: ExpenseEditorUiState,
    modifier: Modifier = Modifier,
) {
    val modeLabels = mapOf(
        SplitMode.EQUAL to stringResource(Res.string.split_mode_equal),
        SplitMode.EXACT to stringResource(Res.string.split_mode_exact),
        SplitMode.PERCENTAGE to stringResource(Res.string.split_mode_percentage),
    )
    val modeHint = when (state.splitMode) {
        SplitMode.EQUAL -> stringResource(Res.string.split_equal_hint)
        SplitMode.EXACT -> stringResource(Res.string.split_exact_hint)
        SplitMode.PERCENTAGE -> stringResource(Res.string.split_percentage_hint)
    }
    val amountDigits = state.expenseCurrency?.minorDigits ?: 0
    val saveHint = if (state.isLoading) null else saveBlockerText(state.saveBlocker)
    val statusMessage = splitStatusMessage(state)

    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = AppSpacing.xl, end = AppSpacing.screenPadding, start = AppSpacing.screenPadding, top = AppSpacing.lg)) {
        SectionHeader(label = stringResource(Res.string.expense_details_heading))
        AppTextField(label = stringResource(Res.string.field_expense_title), onValueChange = { onEvent(ExpenseEditorEvent.ChangeTitle(it)) }, value = state.title)
        Spacer(modifier = Modifier.height(AppSpacing.md))
        AppAmountField(fractionDigits = amountDigits, label = stringResource(Res.string.field_amount, state.expenseCurrency?.name.orEmpty()), onValueChange = { onEvent(ExpenseEditorEvent.ChangeAmount(it)) }, value = state.amountText)
        Spacer(modifier = Modifier.height(AppSpacing.md))
        AppPickerField(label = stringResource(Res.string.field_currency), onClick = onPickCurrency, value = state.expenseCurrency?.let { currencyLabel(it) }.orEmpty())
        if (state.isForeignCurrency) {
            Spacer(modifier = Modifier.height(AppSpacing.md))
            AppAmountField(
                fractionDigits = RateFormat.FRACTION_DIGITS,
                label = stringResource(Res.string.field_exchange_rate, state.expenseCurrency?.name.orEmpty(), state.group?.currency?.name.orEmpty()),
                onValueChange = { onEvent(ExpenseEditorEvent.ChangeRate(it)) },
                value = state.rateText,
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.textStyles.listMeta,
                text = state.groupAmount?.let { stringResource(Res.string.exchange_rate_converted, MoneyFormat.format(it)) } ?: stringResource(Res.string.exchange_rate_hint),
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.md))
        AppPickerField(label = stringResource(Res.string.field_paid_by), onClick = onPickPayer, value = state.paidByMember?.let(memberName).orEmpty())
        Spacer(modifier = Modifier.height(AppSpacing.md))
        AppPickerField(label = stringResource(Res.string.field_date), onClick = onPickDate, value = state.spentAt?.let { DateFormat.formatShort(it) }.orEmpty())
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        SectionHeader(label = stringResource(Res.string.expense_split_heading))
        ChoiceRow(label = { mode -> modeLabels.getValue(mode) }, onSelect = { onEvent(ExpenseEditorEvent.ChangeSplitMode(it)) }, options = SplitMode.entries, selected = state.splitMode)
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(style = AppTheme.textStyles.listSecondary, text = modeHint)
        Spacer(modifier = Modifier.height(AppSpacing.md))
        state.members.forEachIndexed { index, member ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(if (state.splitMode == SplitMode.EQUAL) AppSpacing.sm else AppSpacing.md))
            }
            when (state.splitMode) {
                SplitMode.EQUAL -> ParticipantTile(isIncluded = member.id in state.participantIds, name = memberName(member), onToggle = { onEvent(ExpenseEditorEvent.ToggleParticipant(member.id)) }, share = state.shares[member.id])
                SplitMode.EXACT -> AppAmountField(
                    fractionDigits = amountDigits,
                    label = memberName(member),
                    onValueChange = { onEvent(ExpenseEditorEvent.ChangeExactAmount(text = it, memberId = member.id)) },
                    value = state.exactAmountTexts[member.id].orEmpty(),
                )
                SplitMode.PERCENTAGE -> AppAmountField(
                    fractionDigits = PercentFormat.FRACTION_DIGITS,
                    label = stringResource(Res.string.field_percentage, memberName(member)),
                    onValueChange = { onEvent(ExpenseEditorEvent.ChangePercentage(text = it, memberId = member.id)) },
                    value = state.percentageTexts[member.id].orEmpty(),
                )
            }
        }
        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.errorHint, text = statusMessage)
        }
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        if (saveHint != null) {
            Text(modifier = Modifier.fillMaxWidth(), maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.listSecondary.copy(textAlign = TextAlign.Center), text = saveHint)
            Spacer(modifier = Modifier.height(AppSpacing.sm))
        }
        PrimaryButton(modifier = Modifier.fillMaxWidth(), isEnabled = state.canSave, label = stringResource(Res.string.action_save_expense), onClick = { onEvent(ExpenseEditorEvent.Save) })
    }
}

@Composable
private fun saveBlockerText(blocker: SaveBlocker?): String? = when (blocker) {
    SaveBlocker.AMOUNT -> stringResource(Res.string.save_blocker_amount)
    SaveBlocker.EXCHANGE_RATE -> stringResource(Res.string.save_blocker_exchange_rate)
    SaveBlocker.PAYER -> stringResource(Res.string.save_blocker_payer)
    SaveBlocker.SPLIT -> stringResource(Res.string.save_blocker_split)
    SaveBlocker.TITLE -> stringResource(Res.string.save_blocker_title)
    null -> null
}

@Composable
private fun splitStatusMessage(state: ExpenseEditorUiState): String? {
    val amount = state.amount ?: return null
    return when (state.splitMode) {
        SplitMode.EQUAL -> if (state.splitError == SplitError.NO_PARTICIPANTS) stringResource(Res.string.split_error_no_participants) else null
        SplitMode.EXACT -> state.assignedAmount?.let { assigned -> differenceMessage(remaining = amount - assigned) }
        SplitMode.PERCENTAGE -> percentageMessage(remainingBasisPoints = ExpenseSplitter.FULL_BASIS_POINTS - state.assignedBasisPoints)
    }
}

@Composable
private fun differenceMessage(remaining: Money): String? = when {
    remaining.isPositive -> stringResource(Res.string.split_remaining, MoneyFormat.format(remaining))
    remaining.isNegative -> stringResource(Res.string.split_over_total, MoneyFormat.format(-remaining))
    else -> null
}

@Composable
private fun percentageMessage(remainingBasisPoints: Int): String? = when {
    remainingBasisPoints > 0 -> stringResource(Res.string.split_remaining, PercentFormat.format(remainingBasisPoints))
    remainingBasisPoints < 0 -> stringResource(Res.string.split_over_total, PercentFormat.format(-remainingBasisPoints))
    else -> null
}

@Composable
private fun ParticipantTile(isIncluded: Boolean, name: String, onToggle: () -> Unit, share: Money?, modifier: Modifier = Modifier) {
    val shareText = when {
        !isIncluded -> stringResource(Res.string.share_not_included)
        share != null -> MoneyFormat.format(share)
        else -> ""
    }

    AppListTile(
        modifier = modifier,
        icon = if (isIncluded) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
        onClick = onToggle,
        title = name,
    ) {
        Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.amount.copy(color = if (isIncluded) AppColors.textPrimary else AppColors.textTertiary), text = shareText)
    }
}
