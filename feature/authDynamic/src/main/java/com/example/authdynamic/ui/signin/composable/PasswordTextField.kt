package com.example.authdynamic.ui.signin.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.authdynamic.R
import com.example.core.data.models.FieldText
import com.example.core.ui.TextFieldWithError
import com.example.core.utils.OnClick
import com.example.core.utils.OnEnterText

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun PasswordTextField(
    data: FieldText,
    focusRequester: FocusRequester,
    onEnterText: OnEnterText,
    onClickDone: OnClick,
    keyboardController: SoftwareKeyboardController?,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    TextFieldWithError(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .focusRequester(focusRequester),
        data = data,
        hint = "Password",
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        keyboardActions =
            remember {
                KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusRequester.freeFocus()
                    onClickDone()
                })
            },
        onEnterText = onEnterText,
        trailingIcon = {
            val image: Int
            val description: String
            // TODO Please provide localized description for accessibility services
            when {
                passwordVisible -> {
                    image = R.drawable.ic_visibility
                    description = "Hide password"
                }

                else -> {
                    image = R.drawable.ic_visibility_off
                    description = "Show password"
                }
            }

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = ImageVector.vectorResource(id = image), description)
            }
        },
    )
}
