package org.example.evenly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme

@Composable
fun EmptyState(message: String, title: String, icon: ImageVector, modifier: Modifier = Modifier, action: (@Composable ColumnScope.() -> Unit)? = null) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(modifier = Modifier.size(AppSpacing.emptyStateIcon).background(color = AppColors.surfaceSunken, shape = CircleShape), contentAlignment = Alignment.Center) {
            Icon(modifier = Modifier.size(AppSpacing.iconEmptyState), contentDescription = null, imageVector = icon, tint = AppColors.textTertiary)
        }
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(modifier = Modifier.fillMaxWidth(), maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.listPrimary.copy(textAlign = TextAlign.Center), text = title)
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(modifier = Modifier.fillMaxWidth(), style = AppTheme.textStyles.listSecondary.copy(textAlign = TextAlign.Center), text = message)
        if (action != null) {
            Spacer(modifier = Modifier.height(AppSpacing.xl))
            action()
        }
    }
}

@Composable
fun ScreenEmptyState(message: String, title: String, icon: ImageVector, modifier: Modifier = Modifier, action: @Composable ColumnScope.() -> Unit) {
    Box(modifier = modifier.verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
        EmptyState(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.xl),
            action = action,
            icon = icon,
            message = message,
            title = title,
        )
    }
}
