package org.example.evenly.data.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteShare(val position: Int, val value: Long, @SerialName("member_id") val memberId: String)

@Serializable
data class RemoteGroupWrite(
    @SerialName("deleted") val isDeleted: Boolean,
    @SerialName("created_at_ms") val createdAtMs: Long,
    @SerialName("modified_at_ms") val modifiedAtMs: Long,
    @SerialName("currency_code") val currencyCode: String,
    val id: String,
    val name: String,
)

@Serializable
data class RemoteGroupRow(
    @SerialName("deleted") val isDeleted: Boolean,
    @SerialName("created_at_ms") val createdAtMs: Long,
    @SerialName("modified_at_ms") val modifiedAtMs: Long,
    @SerialName("sync_seq") val syncSeq: Long,
    @SerialName("currency_code") val currencyCode: String,
    val id: String,
    @SerialName("invite_code") val inviteCode: String,
    val name: String,
)

@Serializable
data class RemoteMemberWrite(
    @SerialName("deleted") val isDeleted: Boolean,
    val position: Int,
    @SerialName("modified_at_ms") val modifiedAtMs: Long,
    @SerialName("group_id") val groupId: String,
    val id: String,
    val name: String,
)

@Serializable
data class RemoteMemberRow(
    @SerialName("deleted") val isDeleted: Boolean,
    val position: Int,
    @SerialName("modified_at_ms") val modifiedAtMs: Long,
    @SerialName("sync_seq") val syncSeq: Long,
    @SerialName("group_id") val groupId: String,
    val id: String,
    val name: String,
)

@Serializable
data class RemoteExpenseWrite(
    @SerialName("deleted") val isDeleted: Boolean,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("modified_at_ms") val modifiedAtMs: Long,
    @SerialName("spent_at_ms") val spentAtMs: Long,
    @SerialName("exchange_rate_micros") val exchangeRateMicros: Long?,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("group_id") val groupId: String,
    val id: String,
    @SerialName("paid_by_member_id") val paidByMemberId: String,
    @SerialName("split_kind") val splitKind: String,
    val title: String,
    @SerialName("exchange_rate_currency_code") val exchangeRateCurrencyCode: String?,
    val shares: List<RemoteShare>,
)

@Serializable
data class RemoteExpenseRow(
    @SerialName("deleted") val isDeleted: Boolean,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("modified_at_ms") val modifiedAtMs: Long,
    @SerialName("spent_at_ms") val spentAtMs: Long,
    @SerialName("sync_seq") val syncSeq: Long,
    @SerialName("exchange_rate_micros") val exchangeRateMicros: Long?,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("group_id") val groupId: String,
    val id: String,
    @SerialName("paid_by_member_id") val paidByMemberId: String,
    @SerialName("split_kind") val splitKind: String,
    val title: String,
    @SerialName("exchange_rate_currency_code") val exchangeRateCurrencyCode: String?,
    val shares: List<RemoteShare>,
)

@Serializable
data class RemoteSettlementWrite(
    @SerialName("deleted") val isDeleted: Boolean,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("modified_at_ms") val modifiedAtMs: Long,
    @SerialName("settled_at_ms") val settledAtMs: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("from_member_id") val fromMemberId: String,
    @SerialName("group_id") val groupId: String,
    val id: String,
    @SerialName("to_member_id") val toMemberId: String,
)

@Serializable
data class RemoteSettlementRow(
    @SerialName("deleted") val isDeleted: Boolean,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("modified_at_ms") val modifiedAtMs: Long,
    @SerialName("settled_at_ms") val settledAtMs: Long,
    @SerialName("sync_seq") val syncSeq: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("from_member_id") val fromMemberId: String,
    @SerialName("group_id") val groupId: String,
    val id: String,
    @SerialName("to_member_id") val toMemberId: String,
)
