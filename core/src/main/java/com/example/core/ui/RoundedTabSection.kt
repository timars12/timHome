package com.example.core.ui

import androidx.compose.foundation.background
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
import com.example.core.ui.theme.BackgroundTabColor
import com.example.core.ui.theme.cornerRoundedShapes
import com.example.core.utils.OnEnterText
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

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
                            color = if (selected) Color.White else Color.Transparent,
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
                        color = if (selected) Color.Black else Color.White,
                    )
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
