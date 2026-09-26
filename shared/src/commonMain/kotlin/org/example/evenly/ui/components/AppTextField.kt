package org.example.evenly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme
import org.example.evenly.util.DecimalInput

private const val GROUP_SIZE: Int = 3

private class GroupedDecimalTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val decimalIndex = raw.indexOf(DecimalInput.DECIMAL_SEPARATOR).let { if (it < 0) raw.length else it }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset + (1..minOf(offset, decimalIndex - 1)).count { (decimalIndex - it) % GROUP_SIZE == 0 }

            override fun transformedToOriginal(offset: Int): Int = (0..raw.length).last { originalToTransformed(it) <= offset }
        }
        return TransformedText(AnnotatedString(DecimalInput.groupThousands(raw.substring(0, decimalIndex)) + raw.substring(decimalIndex)), offsetMapping)
    }
}

@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    FieldFrame(modifier = modifier, isFocused = isFocused, label = label, onTap = { focusRequester.requestFocus() }, textAlign = TextAlign.Start) {
        BasicTextField(
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged { isFocused = it.isFocused },
            cursorBrush = SolidColor(AppColors.primary),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions(capitalization = capitalization, imeAction = ImeAction.Done, keyboardType = keyboardType),
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = AppTheme.textStyles.fieldValue,
            value = value,
        )
    }
}

@Composable
fun AppAmountField(fractionDigits: Int, label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val currentValue by rememberUpdatedState(value)
    var isFocused by remember { mutableStateOf(false) }
    var selection by remember { mutableStateOf(TextRange(value.length)) }
    val visualTransformation = remember { GroupedDecimalTransformation() }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            withFrameNanos {}
            selection = TextRange(0, currentValue.length)
        }
    }

    FieldFrame(modifier = modifier, isFocused = isFocused, label = label, onTap = { focusRequester.requestFocus() }, textAlign = TextAlign.End) {
        BasicTextField(
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged { isFocused = it.isFocused },
            cursorBrush = SolidColor(AppColors.primary),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Decimal),
            onValueChange = { next ->
                val sanitized = DecimalInput.sanitize(fractionDigits = fractionDigits, previousText = value, text = next.text)
                selection = if (sanitized == next.text) next.selection else TextRange(sanitized.length)
                if (sanitized != value) {
                    onValueChange(sanitized)
                }
            },
            singleLine = true,
            textStyle = AppTheme.textStyles.amount.copy(textAlign = TextAlign.End),
            value = TextFieldValue(selection = TextRange(selection.start.coerceIn(0, value.length), selection.end.coerceIn(0, value.length)), text = value),
            visualTransformation = visualTransformation,
        )
    }
}

@Composable
fun AppPickerField(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FieldFrame(
        modifier = modifier,
        label = label,
        onClick = onClick,
        textAlign = TextAlign.Start,
        trailing = {
            Icon(modifier = Modifier.size(AppSpacing.iconSheet), contentDescription = null, imageVector = Icons.Outlined.ExpandMore, tint = AppColors.primary)
        },
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = AppTheme.textStyles.fieldValue,
            text = value,
        )
    }
}

@Composable
private fun FieldFrame(
    label: String,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    onClick: (() -> Unit)? = null,
    onTap: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val currentOnTap by rememberUpdatedState(onTap)
    val shape = RoundedCornerShape(AppSpacing.radiusField)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppSpacing.controlHeight)
            .clip(shape)
            .background(color = AppColors.surfaceField, shape = shape)
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick, role = Role.Button))
            .then(if (onTap == null) Modifier else Modifier.pointerInput(Unit) { detectTapGestures { currentOnTap?.invoke() } })
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.textStyles.fieldLabel.copy(color = if (isFocused) AppColors.primary else AppColors.textSecondary, textAlign = textAlign),
                text = label,
            )
            content()
        }
        if (trailing != null) {
            trailing()
        }
    }
}
