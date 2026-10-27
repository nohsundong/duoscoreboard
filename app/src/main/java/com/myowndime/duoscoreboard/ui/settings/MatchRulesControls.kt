package com.myowndime.duoscoreboard.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myowndime.duoscoreboard.R
import com.myowndime.duoscoreboard.domain.model.AppSettings

/**
 * The three match-rule choices (sets to win the match, points to win a set, deuce).
 * Shared between the settings screen and the first-run setup dialog so both stay in sync.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchRulesControls(
    matchFormatSets: Int,
    setTargetScore: Int,
    deuceEnabled: Boolean,
    onMatchFormatChange: (Int) -> Unit,
    onSetTargetChange: (Int) -> Unit,
    onDeuceChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.settings_match_format),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_match_format_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppSettings.MATCH_FORMAT_OPTIONS.forEach { format ->
                    val label = when (format) {
                        AppSettings.MATCH_FORMAT_MANUAL ->
                            stringResource(R.string.settings_match_format_manual)
                        1 -> stringResource(R.string.settings_match_format_single)
                        else -> pluralStringResource(
                            R.plurals.settings_match_format_best_of,
                            AppSettings.setsToWinMatch(format),
                            format,
                            AppSettings.setsToWinMatch(format),
                        )
                    }
                    FilterChip(
                        selected = matchFormatSets == format,
                        onClick = { onMatchFormatChange(format) },
                        label = { Text(label) },
                        modifier = Modifier.testTag(SettingsTestTags.matchFormat(format)),
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.settings_set_target),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_set_target_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppSettings.SET_TARGET_OPTIONS.forEach { target ->
                    val label = if (target == AppSettings.SET_TARGET_MANUAL) {
                        stringResource(R.string.settings_set_target_manual)
                    } else {
                        target.toString()
                    }
                    FilterChip(
                        selected = setTargetScore == target,
                        onClick = { onSetTargetChange(target) },
                        label = { Text(label) },
                        modifier = Modifier.testTag(SettingsTestTags.setTarget(target)),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_deuce),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.settings_deuce_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = deuceEnabled,
                onCheckedChange = onDeuceChange,
                enabled = setTargetScore != AppSettings.SET_TARGET_MANUAL,
                modifier = Modifier.testTag(SettingsTestTags.DEUCE_ENABLED),
            )
        }
    }
}
