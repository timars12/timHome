package com.example.authdynamic.ui.signin.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core.utils.OnClick
import com.example.core.utils.OnEnterText

@Composable
fun PasswordTextField(
    password: String?,
    focusManager: FocusManager,
    onEnterText: OnEnterText,
    onClickDone: OnClick
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    TextField(
        value = password ?: "",
        onValueChange = onEnterText,
        singleLine = true,
        placeholder = {
            Text(text = "Password")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible) {
                Icons.Filled.ArrowBack
            } else {
                Icons.Filled.Warning
            }

            // Please provide localized description for accessibility services
            val description = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, description)
            }
        },
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            onClickDone()
        })
    )
}
