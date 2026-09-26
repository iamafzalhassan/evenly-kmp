package org.example.evenly.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object CreateGroupRoute

@Serializable
data class ExpenseEditorRoute(val groupId: String, val expenseId: String? = null)

@Serializable
data class GroupRoute(val groupId: String)

@Serializable
data object GroupsRoute

@Serializable
data object SettingsRoute
