package com.khz.malekclient.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import com.khz.malekclient.ui.theme.DesignTokens
import com.khz.malekclient.ui.theme.GlassBorder
import com.khz.malekclient.ui.theme.GoldPrimary
import com.khz.malekclient.ui.theme.WhiteTransparent08

/** Text field with a persistent label and consistent dimensions for client forms. */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    enabled: Boolean = true,
    height: Dp = DesignTokens.InputHeight
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(height),
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        placeholder = placeholder?.let { hint ->
            { Text(hint, style = MaterialTheme.typography.bodyMedium) }
        },
        textStyle = MaterialTheme.typography.bodySmall,
        singleLine = true,
        enabled = enabled,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        shape = RoundedCornerShape(DesignTokens.ControlRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = WhiteTransparent08,
            unfocusedContainerColor = WhiteTransparent08,
            disabledContainerColor = WhiteTransparent08,
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = GlassBorder,
            disabledBorderColor = GlassBorder.copy(alpha = 0.5f),
            focusedLabelColor = GoldPrimary,
            unfocusedLabelColor = Color.White.copy(alpha = 0.65f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White.copy(alpha = 0.55f),
            cursorColor = GoldPrimary,
            focusedPlaceholderColor = Color.White.copy(alpha = 0.45f),
            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.45f)
        )
    )
}
