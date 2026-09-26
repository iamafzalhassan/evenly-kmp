package org.example.evenly.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme

@Composable
fun AppListTile(title: String, icon: ImageVector, modifier: Modifier = Modifier, subtitle: String? = null, onClick: (() -> Unit)? = null, trailing: @Composable RowScope.() -> Unit = {}) {
    val shape = RoundedCornerShape(AppSpacing.radiusCard)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color = AppColors.surfaceCard, shape = shape)
            .border(border = BorderStroke(AppSpacing.hairline, AppColors.divider), shape = shape)
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick, role = Role.Button))
            .padding(AppSpacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(AppSpacing.tileIcon).background(color = AppColors.surfaceSunken, shape = CircleShape), contentAlignment = Alignment.Center) {
            Icon(modifier = Modifier.size(AppSpacing.iconSheet), contentDescription = null, imageVector = icon, tint = AppColors.primary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.listPrimary, text = title)
            if (subtitle != null) {
                Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.listMeta, text = subtitle)
            }
        }
        trailing()
    }
}
