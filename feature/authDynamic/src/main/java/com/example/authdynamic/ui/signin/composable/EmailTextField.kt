package com.example.authdynamic.ui.signin.composable

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.data.models.FieldText
import com.example.core.ui.TextFieldWithError
import com.example.core.utils.OnEnterText
import com.example.core.utils.mvi.ErrorType
import com.example.core.utils.mvi.MviError

@Composable
internal fun EmailTextField(
    modifier: Modifier = Modifier,
    data: FieldText,
    focusRequester: FocusRequester,
    onEnterText: OnEnterText,
) {
    TextFieldWithError(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        data = data,
        hint = "Email",
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        keyboardActions = KeyboardActions(onNext = { focusRequester.requestFocus() }),
        onEnterText = onEnterText,
    )
}

@Preview(name = "Light Mode Empty Field")
@Composable
fun TestEmailTextFieldEmpty() {
    EmailTextField(
        modifier = Modifier,
        data = FieldText("", null),
        FocusRequester(),
    ) {}
}

@Preview(name = "Light Mode")
@Composable
fun TestEmailTextField() {
    EmailTextField(
        modifier = Modifier,
        FieldText("tim@dfff", MviError(type = ErrorType.FIELD, "Something wrong")),
        FocusRequester(),
    ) {}
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TestEmailTextFieldDarkPreview() {
    EmailTextField(
        modifier = Modifier,
        FieldText("tim@df", MviError(type = ErrorType.FIELD, "Something wrong")),
        FocusRequester(),
    ) {}
}
