package org.example.evenly.feature.group.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.action_close
import evenly.shared.generated.resources.action_replace_code
import evenly.shared.generated.resources.action_share_code
import evenly.shared.generated.resources.invite_message
import evenly.shared.generated.resources.invite_pending
import evenly.shared.generated.resources.invite_share_text
import evenly.shared.generated.resources.invite_title
import org.example.evenly.core.designsystem.components.AppModalSheet
import org.example.evenly.core.designsystem.components.PrimaryButton
import org.example.evenly.core.designsystem.components.SecondaryButton
import org.example.evenly.core.designsystem.components.SheetActions
import org.example.evenly.core.designsystem.theme.AppColors
import org.example.evenly.core.designsystem.theme.AppSpacing
import org.example.evenly.core.designsystem.theme.AppTheme
import org.example.evenly.core.platform.rememberTextSharer
import org.jetbrains.compose.resources.stringResource

@Composable
fun InviteSheet(groupName: String, inviteCode: String?, onDismiss: () -> Unit, onReplaceCode: () -> Unit, modifier: Modifier = Modifier) {
    val shareText = rememberTextSharer()
    val shareMessage = inviteCode?.let { stringResource(Res.string.invite_share_text, groupName, it) }

    AppModalSheet(modifier = modifier, onDismiss = onDismiss, title = stringResource(Res.string.invite_title)) { sheet ->
        Text(modifier = Modifier.fillMaxWidth(), style = AppTheme.textStyles.body, text = stringResource(if (inviteCode == null) Res.string.invite_pending else Res.string.invite_message))
        if (inviteCode != null) {
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            Box(modifier = Modifier.fillMaxWidth().background(color = AppColors.surfaceField, shape = RoundedCornerShape(AppSpacing.radiusCard)).padding(vertical = AppSpacing.lg), contentAlignment = Alignment.Center) {
                Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.inviteCode, text = inviteCode)
            }
            Spacer(modifier = Modifier.height(AppSpacing.md))
            SecondaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(Res.string.action_replace_code), onClick = { sheet.closeThen(onReplaceCode) })
        }
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        SheetActions(dismissLabel = stringResource(Res.string.action_close), onDismiss = onDismiss, sheet = sheet) {
            PrimaryButton(
                modifier = Modifier.weight(1f),
                isEnabled = shareMessage != null,
                label = stringResource(Res.string.action_share_code),
                onClick = {
                    if (shareMessage != null) {
                        sheet.closeThen {
                            onDismiss()
                            shareText(shareMessage)
                        }
                    }
                },
            )
        }
    }
}
