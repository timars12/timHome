package com.example.authdynamic.ui.signin.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.ui.RoundedTabSection
import com.example.core.ui.theme.AuthTabSectionBackgroundColor
import com.example.core.utils.OnEnterText
import com.example.core.utils.rememberImeState
import kotlinx.collections.immutable.PersistentList

@Composable
fun AuthTabSection(
    tabNameList: PersistentList<String>,
    selectedTab: OnEnterText,
) {
    val isKeyboardShow by rememberImeState()
    val animatedTabSectionRatio by animateFloatAsState(
        targetValue = if (isKeyboardShow) 0f else 0.3f,
        label = "",
    )
    AnimatedVisibility(visible = !isKeyboardShow) {
        Column(
            modifier =
                Modifier
                    .padding(horizontal = 24.dp, vertical = 48.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(animatedTabSectionRatio)
                    .background(
                        color = AuthTabSectionBackgroundColor,
                        shape = RoundedCornerShape(24.dp),
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            RoundedTabSection(
                modifier = Modifier.padding(horizontal = 36.dp),
                tabNameList = tabNameList,
                selectedTab = selectedTab,
            )
        }
    }
}
