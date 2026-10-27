package com.myowndime.duoscoreboard.ui.common

import android.content.Context
import android.content.Intent

/**
 * Hands plain text to the system share sheet.
 *
 * The app never sends anything itself - the user picks the target app and that app
 * does the sending, which is why no INTERNET permission is involved.
 */
fun shareText(context: Context, text: String, chooserTitle: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
}
