package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.ui.theme.AuthTabSectionBackgroundColor
import com.example.core.ui.theme.BackgroundTabColor
import com.example.core.ui.theme.cornerRoundedShapes
import com.example.core.utils.OnEnterText
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun RoundedTabSection(
    modifier: Modifier = Modifier,
    tabNameList: PersistentList<String>,
    selectedTab: OnEnterText,
) {
    val selectedIndex = remember { mutableIntStateOf(0) }

    TabRow(
        selectedTabIndex = selectedIndex.intValue,
        containerColor = BackgroundTabColor,
        modifier = modifier.clip(MaterialTheme.cornerRoundedShapes.rounded),
        indicator = { },
        divider = { },
    ) {
        tabNameList.forEachIndexed { index, text ->
            val selected = selectedIndex.intValue == index
            Tab(
                modifier =
                    Modifier
                        .padding(4.dp)
                        .background(
                            color = if (selected) AuthTabSectionBackgroundColor else Color.Transparent,
                            shape = MaterialTheme.cornerRoundedShapes.rounded,
                        ),
                selected = selected,
                onClick = {
                    selectedTab(text)
                    selectedIndex.intValue = index
                },
                text = {
                    Text(
                        text = text,
                        color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.inversePrimary,
                    )
                },
                interactionSource =
                    remember {
                        object : MutableInteractionSource {
                            override val interactions: Flow<Interaction> = emptyFlow()

                            override suspend fun emit(interaction: Interaction) {}

                            override fun tryEmit(interaction: Interaction) = true
                        }
                    },
            )
        }
    }
}

@Preview
@Composable
fun PreviewRoundedTabSection() {
    val list = persistentListOf("Active", "Completed")
    RoundedTabSection(tabNameList = list) {}
}
