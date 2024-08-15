package com.example.core.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import com.example.core.ui.theme.Black
import com.example.core.ui.theme.IndicatorCO2Danger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ExpandableText(
    text: String,
    maxLines: Int = 2,
    durationMillis: Int = 300,
) {
    var expanded by remember { mutableStateOf(false) }

    val textLines = remember { mutableIntStateOf(0) }
    val showButton = textLines.intValue > maxLines
    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }

    Column {
        Text(
            text = text,
            maxLines = if (expanded || !showButton) Int.MAX_VALUE else maxLines,
            modifier = Modifier.animateContentSize(animationSpec = tween(durationMillis)),
            onTextLayout = { textLayoutResultState.value = it },
            color = Black,
        )

        LaunchedEffect(text) {
            textLines.intValue =
                withContext(Dispatchers.Default) {
                    // Use TextLayoutResult to calculate the number of lines
                    val textLayoutResult = textLayoutResultState.value
                    textLayoutResult?.lineCount ?: 0
                }
        }

        if (showButton) {
            Text(
                modifier = Modifier.clickable { expanded = !expanded },
                text =
                    when (expanded) {
                        true -> "Show less..."
                        else -> "Show more..."
                    },
                color = IndicatorCO2Danger,
            )
        }
    }
}

// @Composable
// fun ExpandableText(modifier: Modifier = Modifier, text: String) {
//    var isExpanded by remember { mutableStateOf(false) }
//    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
//    var isClickable by remember { mutableStateOf(false) }
//    var finalText by remember { mutableStateOf(text) }
//
//    val textLayoutResult = textLayoutResultState.value
//    LaunchedEffect(textLayoutResult) {
//        if (textLayoutResult == null) return@LaunchedEffect
//        when {
//            isExpanded -> finalText = text
//            textLayoutResult.hasVisualOverflow -> {
//                val lastCharIndex = textLayoutResult.getLineEnd(1)
//                val showMoreString = "... Show More" // TODO add to res and add span text
//                val adjustedText = text
//                    .substring(startIndex = 0, endIndex = lastCharIndex)
//                    .dropLast(showMoreString.length)
//                    .dropLastWhile { it == ' ' || it == '.' }
//
//                finalText = "$adjustedText$showMoreString"
//                isClickable = true
//            }
//        }
//    }
//
//    Text(
//        text = finalText,
//        style = MaterialTheme.typography.body2,
//        color = Color.Black,
//        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
//        onTextLayout = { textLayoutResultState.value = it },
//        modifier = modifier
//            .clickable(enabled = isClickable) { isExpanded = !isExpanded }
//            .animateContentSize(),
//    )
// }
