package com.myowndime.duoscoreboard.ui.theme

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.myowndime.duoscoreboard.R

/** A selectable panel color with a localized, screen-reader friendly label. */
data class PaletteColor(
    val argb: Long,
    @param:StringRes val labelRes: Int,
)

/**
 * Fixed palette instead of a free color picker: every entry is dark or light enough
 * to reach a readable contrast ratio with the score text drawn on top of it.
 */
val PlayerPalette: List<PaletteColor> = listOf(
    PaletteColor(0xFF1565C0, R.string.color_blue),
    PaletteColor(0xFFC62828, R.string.color_red),
    PaletteColor(0xFF2E7D32, R.string.color_green),
    PaletteColor(0xFFF9A825, R.string.color_amber),
    PaletteColor(0xFF6A1B9A, R.string.color_purple),
    PaletteColor(0xFF00695C, R.string.color_teal),
    PaletteColor(0xFFEF6C00, R.string.color_orange),
    PaletteColor(0xFF37474F, R.string.color_slate),
)

/** Black or white, whichever keeps text legible on [background]. */
fun contentColorOn(background: Color): Color =
    if (background.luminance() > 0.45f) Color(0xFF10161C) else Color.White
