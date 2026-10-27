package com.myowndime.duoscoreboard.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Two opposing arrows (Material "swap horizontal"). Defined here because it lives in the
 * extended icon set, and pulling in that whole artifact for one glyph would bloat the app.
 */
val SwapHorizIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "SwapHoriz",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser()
            .parsePathString("M6.99,11L3,15l3.99,4v-3H14v-2H6.99v-3zM21,9l-3.99,-4v3H10v2h7.01v3L21,9z")
            .toNodes(),
        fill = SolidColor(Color.Black),
    ).build()
}
