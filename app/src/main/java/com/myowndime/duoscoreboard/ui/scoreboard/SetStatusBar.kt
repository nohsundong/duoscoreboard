package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.myowndime.duoscoreboard.R
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.ui.theme.DuoScoreboardTheme

/**
 * A glanceable strip showing which sets each player has already won, and the match
 * format they're playing under. Sits above the score panels so a mid-game glance
 * doesn't require reading the smaller text inside each panel.
 */
@Composable
fun SetStatusBar(
    playerOneName: String,
    playerOneSets: Int,
    playerOneColor: Color,
    playerTwoName: String,
    playerTwoSets: Int,
    playerTwoColor: Color,
    matchFormatSets: Int,
    modifier: Modifier = Modifier,
    swapped: Boolean = false,
) {
    val setsToWin = AppSettings.setsToWinMatch(matchFormatSets)
    val centerLabel = when {
        setsToWin <= 0 -> stringResource(R.string.set_status_manual_label)
        matchFormatSets == 1 -> stringResource(R.string.settings_match_format_single)
        else -> pluralStringResource(
            R.plurals.settings_match_format_best_of,
            setsToWin,
            matchFormatSets,
            setsToWin,
        )
    }
    val statusDescription = stringResource(
        R.string.set_status_description,
        playerOneName,
        playerOneSets,
        playerTwoName,
        playerTwoSets,
        centerLabel,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) { contentDescription = statusDescription }
            .testTag(ScoreboardTestTags.SET_STATUS_BAR),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Matches the panel order below it, so each side's dots sit above its own panel.
        SetStatusSide(
            name = if (swapped) playerTwoName else playerOneName,
            sets = if (swapped) playerTwoSets else playerOneSets,
            setsToWin = setsToWin,
            color = if (swapped) playerTwoColor else playerOneColor,
            alignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .testTag(ScoreboardTestTags.sets(if (swapped) Player.TWO else Player.ONE)),
        )
        Text(
            text = centerLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        SetStatusSide(
            name = if (swapped) playerOneName else playerTwoName,
            sets = if (swapped) playerOneSets else playerTwoSets,
            setsToWin = setsToWin,
            color = if (swapped) playerOneColor else playerTwoColor,
            alignment = Alignment.End,
            modifier = Modifier
                .weight(1f)
                .testTag(ScoreboardTestTags.sets(if (swapped) Player.ONE else Player.TWO)),
        )
    }
}

@Composable
private fun SetStatusSide(
    name: String,
    sets: Int,
    setsToWin: Int,
    color: Color,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (setsToWin > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(setsToWin) { index ->
                    SetPip(color = color, filled = index < sets)
                }
            }
        } else {
            Text(
                text = stringResource(R.string.sets_label, sets),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
            )
        }
    }
}

@Composable
private fun SetPip(color: Color, filled: Boolean) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .then(
                if (filled) {
                    Modifier.background(color, CircleShape)
                } else {
                    Modifier.border(1.dp, color.copy(alpha = 0.45f), CircleShape)
                },
            ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SetStatusBarBestOfPreview() {
    DuoScoreboardTheme {
        SetStatusBar(
            playerOneName = "Player 1",
            playerOneSets = 2,
            playerOneColor = Color(0xFF1565C0),
            playerTwoName = "Player 2",
            playerTwoSets = 1,
            playerTwoColor = Color(0xFFC62828),
            matchFormatSets = 5,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SetStatusBarManualPreview() {
    DuoScoreboardTheme {
        SetStatusBar(
            playerOneName = "Player 1",
            playerOneSets = 3,
            playerOneColor = Color(0xFF1565C0),
            playerTwoName = "Player 2",
            playerTwoSets = 2,
            playerTwoColor = Color(0xFFC62828),
            matchFormatSets = AppSettings.MATCH_FORMAT_MANUAL,
        )
    }
}
