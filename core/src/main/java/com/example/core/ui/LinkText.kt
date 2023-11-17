package com.example.core.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.LinkTextColor

@Composable
fun LinkText(text: String) {
    val context = LocalContext.current

    Text(
        modifier = Modifier.clickable { openBrowser(context, text) },
        text = text,
        color = LinkTextColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.W400,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
private fun openBrowser(context: Context, link: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    context.startActivity(intent)
}
