package com.example.authdynamic.ui.signin.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.utils.OnEnterText

@Composable
fun EmailTextField(text: String?, focusManager: FocusManager, onEnterText: OnEnterText) {
    TextField(
        value = text ?: "",
        onValueChange = onEnterText,
        singleLine = true,
        placeholder = {
            Text(text = "Email")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = {
            focusManager.moveFocus(FocusDirection.Down)
        })
    )
}
