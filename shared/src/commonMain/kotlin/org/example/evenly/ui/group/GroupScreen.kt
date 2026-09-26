package org.example.evenly.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.action_add
import evenly.shared.generated.resources.action_add_expense
import evenly.shared.generated.resources.action_add_member
import evenly.shared.generated.resources.action_back_to_groups
import evenly.shared.generated.resources.action_delete_group
import evenly.shared.generated.resources.action_delete_payment
import evenly.shared.generated.resources.action_invite
import evenly.shared.generated.resources.action_record_payment
import evenly.shared.generated.resources.action_remove_member
import evenly.shared.generated.resources.action_rename_group
import evenly.shared.generated.resources.action_save
import evenly.shared.generated.resources.add_member_title
import evenly.shared.generated.resources.balance_gets_back
import evenly.shared.generated.resources.balance_owes
import evenly.shared.generated.resources.balance_settled
import evenly.shared.generated.resources.delete_group_message
import evenly.shared.generated.resources.delete_payment_message
import evenly.shared.generated.resources.error_member_name_taken
import evenly.shared.generated.resources.expense_tile_subtitle
import evenly.shared.generated.resources.expense_tile_subtitle_converted
import evenly.shared.generated.resources.field_group_name
import evenly.shared.generated.resources.field_member_name
import evenly.shared.generated.resources.field_search_expenses
import evenly.shared.generated.resources.group_action_failed
import evenly.shared.generated.resources.group_balances_heading
import evenly.shared.generated.resources.group_expenses_empty_message
import evenly.shared.generated.resources.group_expenses_empty_title
import evenly.shared.generated.resources.group_expenses_heading
import evenly.shared.generated.resources.group_members_heading
import evenly.shared.generated.resources.group_missing_message
import evenly.shared.generated.resources.group_missing_title
import evenly.shared.generated.resources.group_payments_heading
import evenly.shared.generated.resources.group_renamed
import evenly.shared.generated.resources.group_settle_up_heading
import evenly.shared.generated.resources.member_added
import evenly.shared.generated.resources.member_in_use_message
import evenly.shared.generated.resources.member_removed
import evenly.shared.generated.resources.member_too_few_message
import evenly.shared.generated.resources.member_unknown
import evenly.shared.generated.resources.payment_deleted
import evenly.shared.generated.resources.payment_recorded
import evenly.shared.generated.resources.record_payment_message
import evenly.shared.generated.resources.remove_member_message
import evenly.shared.generated.resources.search_no_results
import evenly.shared.generated.resources.settlement_title
import evenly.shared.generated.resources.summary_expense_count
import evenly.shared.generated.resources.summary_paid_back
import evenly.shared.generated.resources.summary_total_spent
import evenly.shared.generated.resources.sync_failed
import evenly.shared.generated.resources.transfer_subtitle
import evenly.shared.generated.resources.transfer_title
import kotlinx.coroutines.launch
import org.example.evenly.domain.ExpenseValuation
import org.example.evenly.model.Balance
import org.example.evenly.model.Expense
import org.example.evenly.model.ExpenseId
import org.example.evenly.model.Member
import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import org.example.evenly.model.Settlement
import org.example.evenly.model.Transfer
import org.example.evenly.ui.components.AppListTile
import org.example.evenly.ui.components.AppPullToRefreshBox
import org.example.evenly.ui.components.AppSnackbarHost
import org.example.evenly.ui.components.AppTextField
import org.example.evenly.ui.components.AppTopBar
import org.example.evenly.ui.components.ConfirmSheet
import org.example.evenly.ui.components.EmptyState
import org.example.evenly.ui.components.PrimaryButton
import org.example.evenly.ui.components.ScreenEmptyState
import org.example.evenly.ui.components.SecondaryButton
import org.example.evenly.ui.components.SectionHeader
import org.example.evenly.ui.components.TextInputSheet
import org.example.evenly.ui.components.TotalsBlock
import org.example.evenly.ui.components.TotalsLine
import org.example.evenly.ui.components.rememberAppSnackbarState
import org.example.evenly.ui.group.components.InviteSheet
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme
import org.example.evenly.util.DateFormat
import org.example.evenly.util.MoneyFormat
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun GroupScreen(onAddExpense: () -> Unit, onBack: () -> Unit, onGroupDeleted: () -> Unit, onEditExpense: (ExpenseId) -> Unit, viewModel: GroupViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarState = rememberAppSnackbarState()
    val nameTakenMessage = stringResource(Res.string.error_member_name_taken)
    val unknownMember = stringResource(Res.string.member_unknown)
    var isAddingMember by rememberSaveable { mutableStateOf(false) }
    var isConfirmingGroupDelete by rememberSaveable { mutableStateOf(false) }
    var isInviting by rememberSaveable { mutableStateOf(false) }
    var isRenamingGroup by rememberSaveable { mutableStateOf(false) }
    var pendingMember by remember { mutableStateOf<Member?>(null) }
    var pendingSettlement by remember { mutableStateOf<Settlement?>(null) }
    var pendingTransfer by remember { mutableStateOf<Transfer?>(null) }
    val group = state.group
    val memberNames = state.memberNames
    val memberName: (MemberId) -> String = { memberId -> memberNames[memberId] ?: unknownMember }

    LaunchedEffect(state.feedback) {
        val feedback = state.feedback ?: return@LaunchedEffect
        if (feedback == GroupFeedback.GROUP_DELETED) {
            onGroupDeleted()
            return@LaunchedEffect
        }
        scope.launch {
            when (feedback) {
                GroupFeedback.ACTION_FAILED -> snackbarState.showError(getString(Res.string.group_action_failed))
                GroupFeedback.GROUP_RENAMED -> snackbarState.showSuccess(getString(Res.string.group_renamed))
                GroupFeedback.MEMBER_ADDED -> snackbarState.showSuccess(getString(Res.string.member_added))
                GroupFeedback.MEMBER_REMOVED -> snackbarState.showSuccess(getString(Res.string.member_removed))
                GroupFeedback.PAYMENT_DELETED -> snackbarState.showSuccess(getString(Res.string.payment_deleted))
                GroupFeedback.PAYMENT_RECORDED -> snackbarState.showSuccess(getString(Res.string.payment_recorded))
                GroupFeedback.SYNC_FAILED -> snackbarState.showError(getString(Res.string.sync_failed))
                GroupFeedback.GROUP_DELETED -> Unit
            }
        }
        viewModel.onEvent(GroupEvent.DismissFeedback)
    }

    Box(modifier = modifier.fillMaxSize().background(AppColors.surfaceBase)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(onBack = onBack, title = group?.name.orEmpty()) {
                if (group != null) {
                    IconButton(onClick = { isInviting = true }) {
                        Icon(contentDescription = stringResource(Res.string.action_invite), imageVector = Icons.Outlined.PersonAddAlt, tint = AppColors.primary)
                    }
                    IconButton(onClick = { isRenamingGroup = true }) {
                        Icon(contentDescription = stringResource(Res.string.action_rename_group), imageVector = Icons.Outlined.Edit, tint = AppColors.primary)
                    }
                    IconButton(onClick = { isConfirmingGroupDelete = true }) {
                        Icon(contentDescription = stringResource(Res.string.action_delete_group), imageVector = Icons.Outlined.Delete, tint = AppColors.primary)
                    }
                }
            }
            if (group != null) {
                AppPullToRefreshBox(modifier = Modifier.weight(1f).fillMaxWidth(), isRefreshing = state.isRefreshing, onRefresh = { viewModel.onEvent(GroupEvent.Refresh) }) {
                    GroupContent(
                        memberName = memberName,
                        onAddExpense = onAddExpense,
                        onAddMember = { isAddingMember = true },
                        onEditExpense = onEditExpense,
                        onExpenseQueryChange = { viewModel.onEvent(GroupEvent.ChangeExpenseQuery(it)) },
                        onSelectMember = { member ->
                            when (state.memberRemoval(member.id)) {
                                MemberRemoval.ALLOWED -> pendingMember = member
                                MemberRemoval.IN_USE -> scope.launch { snackbarState.showBrief(getString(Res.string.member_in_use_message, member.name)) }
                                MemberRemoval.TOO_FEW_MEMBERS -> scope.launch { snackbarState.showBrief(getString(Res.string.member_too_few_message, member.name)) }
                            }
                        },
                        onSelectSettlement = { pendingSettlement = it },
                        onSelectTransfer = { pendingTransfer = it },
                        state = state,
                    )
                }
            } else if (state.isGroupMissing) {
                ScreenEmptyState(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    icon = Icons.Outlined.Groups,
                    message = stringResource(Res.string.group_missing_message),
                    title = stringResource(Res.string.group_missing_title),
                ) {
                    PrimaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(Res.string.action_back_to_groups), onClick = onBack)
                }
            }
        }
        AppSnackbarHost(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(), state = snackbarState)
    }

    if (group == null) return

    if (isInviting) {
        InviteSheet(groupName = group.name, inviteCode = group.inviteCode, onDismiss = { isInviting = false })
    }

    if (isAddingMember) {
        TextInputSheet(
            confirmLabel = stringResource(Res.string.action_add),
            errorMessage = { name -> if (state.isMemberNameAvailable(name)) null else nameTakenMessage },
            initialValue = "",
            label = stringResource(Res.string.field_member_name),
            onConfirm = { name ->
                isAddingMember = false
                viewModel.onEvent(GroupEvent.AddMember(name))
            },
            onDismiss = { isAddingMember = false },
            title = stringResource(Res.string.add_member_title),
        )
    }

    if (isRenamingGroup) {
        TextInputSheet(
            confirmLabel = stringResource(Res.string.action_save),
            errorMessage = { null },
            initialValue = group.name,
            label = stringResource(Res.string.field_group_name),
            onConfirm = { name ->
                isRenamingGroup = false
                viewModel.onEvent(GroupEvent.RenameGroup(name))
            },
            onDismiss = { isRenamingGroup = false },
            title = stringResource(Res.string.action_rename_group),
        )
    }

    if (isConfirmingGroupDelete) {
        ConfirmSheet(
            confirmLabel = stringResource(Res.string.action_delete_group),
            isDestructive = true,
            message = stringResource(Res.string.delete_group_message, group.name),
            onConfirm = {
                isConfirmingGroupDelete = false
                viewModel.onEvent(GroupEvent.DeleteGroup)
            },
            onDismiss = { isConfirmingGroupDelete = false },
            title = stringResource(Res.string.action_delete_group),
        )
    }

    pendingMember?.let { member ->
        ConfirmSheet(
            confirmLabel = stringResource(Res.string.action_remove_member),
            isDestructive = true,
            message = stringResource(Res.string.remove_member_message, member.name),
            onConfirm = {
                pendingMember = null
                viewModel.onEvent(GroupEvent.RemoveMember(member.id))
            },
            onDismiss = { pendingMember = null },
            title = stringResource(Res.string.action_remove_member),
        )
    }

    pendingTransfer?.let { transfer ->
        ConfirmSheet(
            confirmLabel = stringResource(Res.string.action_record_payment),
            message = stringResource(Res.string.record_payment_message, memberName(transfer.from), memberName(transfer.to), MoneyFormat.format(transfer.amount)),
            onConfirm = {
                pendingTransfer = null
                viewModel.onEvent(GroupEvent.RecordTransfer(transfer))
            },
            onDismiss = { pendingTransfer = null },
            title = stringResource(Res.string.action_record_payment),
        )
    }

    pendingSettlement?.let { settlement ->
        ConfirmSheet(
            confirmLabel = stringResource(Res.string.action_delete_payment),
            isDestructive = true,
            message = stringResource(Res.string.delete_payment_message, memberName(settlement.from), memberName(settlement.to), MoneyFormat.format(settlement.amount)),
            onConfirm = {
                pendingSettlement = null
                viewModel.onEvent(GroupEvent.DeleteSettlement(settlement.id))
            },
            onDismiss = { pendingSettlement = null },
            title = stringResource(Res.string.action_delete_payment),
        )
    }
}

@Composable
private fun GroupContent(
    onAddExpense: () -> Unit,
    onAddMember: () -> Unit,
    onEditExpense: (ExpenseId) -> Unit,
    onSelectMember: (Member) -> Unit,
    memberName: (MemberId) -> String,
    onSelectSettlement: (Settlement) -> Unit,
    onExpenseQueryChange: (String) -> Unit,
    onSelectTransfer: (Transfer) -> Unit,
    state: GroupUiState,
    modifier: Modifier = Modifier,
) {
    val summaryLines = summaryLines(state)
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = AppSpacing.xl + navigationBarBottom, end = AppSpacing.screenPadding, start = AppSpacing.screenPadding, top = AppSpacing.lg)) {
        if (state.hasActivity) {
            item {
                TotalsBlock(lines = summaryLines)
                Spacer(modifier = Modifier.height(AppSpacing.xl))
            }
            balanceSection(balances = state.balances, memberName = memberName)
            settleUpSection(memberName = memberName, onSelectTransfer = onSelectTransfer, transfers = state.transfers)
        }
        expenseSection(
            expenseQuery = state.expenseQuery,
            expenses = state.expenses,
            memberName = memberName,
            onEditExpense = onEditExpense,
            onExpenseQueryChange = onExpenseQueryChange,
            visibleExpenses = state.visibleExpenses,
        )
        paymentSection(memberName = memberName, onSelectSettlement = onSelectSettlement, settlements = state.settlements)
        item {
            SectionHeader(label = stringResource(Res.string.group_members_heading))
        }
        items(items = state.group?.members.orEmpty(), key = { member -> "member-${member.id.raw}" }) { member ->
            AppListTile(modifier = Modifier.padding(bottom = AppSpacing.sm), icon = Icons.Outlined.Person, onClick = { onSelectMember(member) }, title = member.name)
        }
        item {
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            SecondaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(Res.string.action_add_member), onClick = onAddMember)
            Spacer(modifier = Modifier.height(AppSpacing.md))
            PrimaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(Res.string.action_add_expense), onClick = onAddExpense)
        }
    }
}

@Composable
private fun summaryLines(state: GroupUiState): List<TotalsLine> {
    val totalSpent = state.totalSpent ?: return emptyList()
    val totalPaidBack = state.totalPaidBack ?: return emptyList()
    return listOf(
        TotalsLine(label = stringResource(Res.string.summary_total_spent), value = MoneyFormat.format(totalSpent)),
        TotalsLine(label = stringResource(Res.string.summary_expense_count), value = state.expenses.size.toString()),
        TotalsLine(label = stringResource(Res.string.summary_paid_back), value = MoneyFormat.format(totalPaidBack)),
    )
}

@Composable
private fun AmountText(money: Money, modifier: Modifier = Modifier, color: Color = AppColors.textPrimary) {
    Text(modifier = modifier, maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.amount.copy(color = color), text = MoneyFormat.format(money))
}

private fun LazyListScope.balanceSection(balances: List<Balance>, memberName: (MemberId) -> String) = section(Res.string.group_balances_heading) {
    items(items = balances, key = { balance -> "balance-${balance.memberId.raw}" }) { balance ->
        val status = when {
            balance.net.isPositive -> stringResource(Res.string.balance_gets_back)
            balance.net.isNegative -> stringResource(Res.string.balance_owes)
            else -> stringResource(Res.string.balance_settled)
        }
        val color = when {
            balance.net.isPositive -> AppColors.success
            balance.net.isNegative -> AppColors.danger
            else -> AppColors.textSecondary
        }

        AppListTile(modifier = Modifier.padding(bottom = AppSpacing.sm), icon = Icons.Outlined.Person, subtitle = status, title = memberName(balance.memberId)) {
            AmountText(color = color, money = balance.net.absolute)
        }
    }
}

private fun LazyListScope.expenseSection(
    expenseQuery: String,
    expenses: List<Expense>,
    visibleExpenses: List<Expense>,
    onEditExpense: (ExpenseId) -> Unit,
    memberName: (MemberId) -> String,
    onExpenseQueryChange: (String) -> Unit,
) = section(Res.string.group_expenses_heading) {
    if (expenses.isEmpty()) {
        item {
            EmptyState(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                message = stringResource(Res.string.group_expenses_empty_message),
                title = stringResource(Res.string.group_expenses_empty_title),
            )
        }
    } else {
        item {
            AppTextField(label = stringResource(Res.string.field_search_expenses), onValueChange = onExpenseQueryChange, value = expenseQuery)
            Spacer(modifier = Modifier.height(AppSpacing.md))
        }
    }
    if (expenses.isNotEmpty() && visibleExpenses.isEmpty()) {
        item {
            Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.listSecondary, text = stringResource(Res.string.search_no_results, expenseQuery.trim()))
            Spacer(modifier = Modifier.height(AppSpacing.sm))
        }
    }
    items(items = visibleExpenses, key = { expense -> "expense-${expense.id.raw}" }) { expense ->
        val date = DateFormat.formatShort(expense.spentAt)
        val converted = expense.exchangeRate?.let { MoneyFormat.format(ExpenseValuation.groupAmount(expense)) }

        AppListTile(
            modifier = Modifier.padding(bottom = AppSpacing.sm),
            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
            onClick = { onEditExpense(expense.id) },
            subtitle = if (converted == null) stringResource(Res.string.expense_tile_subtitle, memberName(expense.paidBy), date) else stringResource(Res.string.expense_tile_subtitle_converted, memberName(expense.paidBy), date, converted),
            title = expense.title,
        ) {
            AmountText(money = expense.amount)
        }
    }
}

private fun LazyListScope.paymentSection(settlements: List<Settlement>, memberName: (MemberId) -> String, onSelectSettlement: (Settlement) -> Unit) {
    if (settlements.isEmpty()) return
    section(Res.string.group_payments_heading) {
        items(items = settlements, key = { settlement -> "settlement-${settlement.id.raw}" }) { settlement ->
            AppListTile(
                modifier = Modifier.padding(bottom = AppSpacing.sm),
                icon = Icons.Outlined.Payments,
                onClick = { onSelectSettlement(settlement) },
                subtitle = DateFormat.formatShort(settlement.settledAt),
                title = stringResource(Res.string.settlement_title, memberName(settlement.from), memberName(settlement.to)),
            ) {
                AmountText(money = settlement.amount)
            }
        }
    }
}

private fun LazyListScope.section(heading: StringResource, content: LazyListScope.() -> Unit) {
    item {
        SectionHeader(label = stringResource(heading))
    }
    content()
    item {
        Spacer(modifier = Modifier.height(AppSpacing.xl))
    }
}

private fun LazyListScope.settleUpSection(transfers: List<Transfer>, memberName: (MemberId) -> String, onSelectTransfer: (Transfer) -> Unit) {
    if (transfers.isEmpty()) return
    section(Res.string.group_settle_up_heading) {
        items(items = transfers, key = { transfer -> "transfer-${transfer.from.raw}-${transfer.to.raw}" }) { transfer ->
            AppListTile(
                modifier = Modifier.padding(bottom = AppSpacing.sm),
                icon = Icons.Outlined.SwapHoriz,
                onClick = { onSelectTransfer(transfer) },
                subtitle = stringResource(Res.string.transfer_subtitle),
                title = stringResource(Res.string.transfer_title, memberName(transfer.from), memberName(transfer.to)),
            ) {
                AmountText(money = transfer.amount)
            }
        }
    }
}
