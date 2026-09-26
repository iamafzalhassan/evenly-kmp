package org.example.evenly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.action_back
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppTopBar(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.surfaceBase)
            .statusBarsPadding()
            .heightIn(min = AppSpacing.topBarHeight)
            .padding(end = AppSpacing.xs, start = if (onBack == null) AppSpacing.screenPadding else AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(contentDescription = stringResource(Res.string.action_back), imageVector = Icons.AutoMirrored.Outlined.ArrowBack, tint = AppColors.primary)
            }
        }
        Text(modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.screenTitle, text = title)
        actions()
    }
}
