package com.khz.malekashtarclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.ui.theme.GlassBorder
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.WhiteTransparent08

/**
 * فیلد ورودی شیشه‌ای — متن، رمز، عدد
 *
 * امضا: GlassTextField(value, onValueChange, label, modifier, isPassword=false, keyboardType=Text, imeAction=Next)
 */
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
    height: Dp = 56.dp
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                color = WhiteTransparent08,
                shape = shape
            )
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = shape
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = androidx.compose.ui.graphics.Color.White
            ),
            cursorBrush = SolidColor(GoldPrimary),
            singleLine = true,
            enabled = enabled,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder ?: label,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    inner()
                }
            }
        )
    }
}
