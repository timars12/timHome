package com.example.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

val Typography =
    Typography(
        headlineLarge =
            TextStyle(
                fontSize = 26.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
            ),
        headlineMedium =
            TextStyle(
                fontSize = 22.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
            ),
        //    subtitle1 = TextStyle(
//        fontFamily = ptSansFontFamily,
//        fontSize = FONT_16.sp,
//        lineHeight = FONT_24.sp,
//        fontWeight = FontWeight.Bold
//    ),
//    body1 = TextStyle(
//        fontFamily = ptSansFontFamily,
//        fontSize = FONT_16.sp,
//        lineHeight = FONT_24.sp
//    ),
        titleMedium =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                letterSpacing = 0.15.sp,
            ),
//    button = TextStyle(
//        fontFamily = ptSansFontFamily,
//        fontSize = FONT_16.sp,
//        lineHeight = FONT_24.sp,
//        fontWeight = FontWeight.Bold
//    ),
//    caption = TextStyle(
//        fontFamily = ptSansFontFamily,
//        fontSize = FONT_12.sp,
//        lineHeight = FONT_20.sp,
//        fontWeight = FontWeight.Bold
//    ),
//    overline = TextStyle(
//        fontFamily = ptSansFontFamily,
//        fontSize = FONT_10.sp,
//        lineHeight = FONT_18.sp,
//        fontWeight = FontWeight.Bold
//    )
    )

// val Typography.caption2: TextStyle
//    get() = TextStyle(
//        fontFamily = ptSansFontFamily,
//        fontSize = FONT_12.sp,
//        lineHeight = FONT_20.sp
//    )

val Typography.chartAxisTextStyle: TextStyle
    get() =
        TextStyle(
            fontSize = 14.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
        )

val Typography.chartValueTextStyle: TextStyle
    get() =
        TextStyle(
            fontSize = 10.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
        )
