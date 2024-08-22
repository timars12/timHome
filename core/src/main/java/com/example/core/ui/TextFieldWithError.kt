package com.example.core.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.data.models.FieldText
import com.example.core.utils.OnEnterText
import com.example.core.utils.mvi.ErrorType
import com.example.core.utils.mvi.MviError

@Composable
fun TextFieldWithError(
    modifier: Modifier = Modifier,
    data: FieldText,
    hint: String,
    singleLine: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    onEnterText: OnEnterText,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Column {
        TextField(
            modifier = modifier,
            value = data.data ?: "",
            singleLine = singleLine,
            trailingIcon = trailingIcon,
            onValueChange = onEnterText,
            isError = data.error != null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            placeholder = { Text(text = hint) },
            visualTransformation = visualTransformation,
            colors =
                TextFieldDefaults.colors().copy(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onTertiary,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onTertiary,
                ),
        )
        if (data.error != null) {
            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                text =
                    data.error.errorMessage
                        ?: stringResource(id = data.error.errorRes!!),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(name = "Light Mode")
@Composable
fun TestEmailTextField() {
    TextFieldWithError(
        data = FieldText("", null),
        hint = "Email",
        onEnterText = {},
        keyboardActions = KeyboardActions { },
        keyboardOptions = KeyboardOptions.Default,
    ) {}
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TestEmailTextFieldDarkPreview() {
    TextFieldWithError(
        data = FieldText("tim@df", MviError(type = ErrorType.FIELD, "Something wrong")),
        hint = "Email",
        onEnterText = {},
        keyboardActions = KeyboardActions { },
        keyboardOptions = KeyboardOptions.Default,
    ) {}
}
