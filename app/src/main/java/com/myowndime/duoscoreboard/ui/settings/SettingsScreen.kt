package com.myowndime.duoscoreboard.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.myowndime.duoscoreboard.R
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.domain.model.PlayerProfile
import com.myowndime.duoscoreboard.domain.model.ThemeMode
import com.myowndime.duoscoreboard.ui.theme.DuoScoreboardTheme
import com.myowndime.duoscoreboard.ui.theme.PlayerPalette
import com.myowndime.duoscoreboard.ui.theme.contentColorOn

object SettingsTestTags {
    const val KEEP_SCREEN_ON: String = "settings_keep_screen_on"
    const val SPEAK_SCORES: String = "settings_speak_scores"
    const val DEUCE_ENABLED: String = "settings_deuce_enabled"
    const val PAD_SCORE_DIGITS: String = "settings_pad_score_digits"

    fun nameField(player: Player): String = "settings_name_${player.name}"

    fun themeOption(mode: ThemeMode): String = "settings_theme_${mode.name}"

    fun setTarget(target: Int): String = "settings_set_target_$target"

    fun matchFormat(sets: Int): String = "settings_match_format_$sets"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onNameChange: (Player, String) -> Unit,
    onColorChange: (Player, Long) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onSpeakScoresChange: (Boolean) -> Unit,
    onSetTargetChange: (Int) -> Unit,
    onDeuceChange: (Boolean) -> Unit,
    onMatchFormatChange: (Int) -> Unit,
    onPadScoreDigitsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            PlayerSection(
                title = stringResource(R.string.settings_player_one),
                player = Player.ONE,
                profile = settings.playerOne,
                placeholder = stringResource(R.string.default_player_one_name),
                onNameChange = onNameChange,
                onColorChange = onColorChange,
            )
            PlayerSection(
                title = stringResource(R.string.settings_player_two),
                player = Player.TWO,
                profile = settings.playerTwo,
                placeholder = stringResource(R.string.default_player_two_name),
                onNameChange = onNameChange,
                onColorChange = onColorChange,
            )

            MatchRulesControls(
                matchFormatSets = settings.matchFormatSets,
                setTargetScore = settings.setTargetScore,
                deuceEnabled = settings.deuceEnabled,
                onMatchFormatChange = onMatchFormatChange,
                onSetTargetChange = onSetTargetChange,
                onDeuceChange = onDeuceChange,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val label = when (mode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                            ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                            ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                        }
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeChange(mode) },
                            label = { Text(label) },
                            modifier = Modifier.testTag(SettingsTestTags.themeOption(mode)),
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
                        text = stringResource(R.string.settings_pad_score),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_pad_score_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.padScoreDigits,
                    onCheckedChange = onPadScoreDigitsChange,
                    modifier = Modifier.testTag(SettingsTestTags.PAD_SCORE_DIGITS),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.settings_keep_screen_on),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = settings.keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange,
                    modifier = Modifier.testTag(SettingsTestTags.KEEP_SCREEN_ON),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_speak_scores),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_speak_scores_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.speakScores,
                    onCheckedChange = onSpeakScoresChange,
                    modifier = Modifier.testTag(SettingsTestTags.SPEAK_SCORES),
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_privacy_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.settings_privacy_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerSection(
    title: String,
    player: Player,
    profile: PlayerProfile,
    placeholder: String,
    onNameChange: (Player, String) -> Unit,
    onColorChange: (Player, Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = profile.name,
            onValueChange = { value ->
                onNameChange(player, value.take(AppSettings.MAX_NAME_LENGTH))
            },
            label = { Text(stringResource(R.string.settings_name_label)) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(SettingsTestTags.nameField(player)),
        )
        Text(
            text = stringResource(R.string.settings_color_label),
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayerPalette.forEach { paletteColor ->
                val selected = profile.colorArgb == paletteColor.argb
                val label = stringResource(
                    R.string.settings_color_option,
                    stringResource(paletteColor.labelRes),
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(paletteColor.argb), CircleShape)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape,
                        )
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onColorChange(player, paletteColor.argb) },
                        )
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = contentColorOn(Color(paletteColor.argb)),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    DuoScoreboardTheme {
        SettingsScreen(
            settings = AppSettings(),
            onNameChange = { _, _ -> },
            onColorChange = { _, _ -> },
            onThemeChange = {},
            onKeepScreenOnChange = {},
            onSpeakScoresChange = {},
            onSetTargetChange = {},
            onDeuceChange = {},
            onMatchFormatChange = {},
            onPadScoreDigitsChange = {},
            onBack = {},
        )
    }
}
