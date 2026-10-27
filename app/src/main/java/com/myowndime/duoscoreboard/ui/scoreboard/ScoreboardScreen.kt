package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.myowndime.duoscoreboard.R
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.ui.common.ConfirmDialog
import com.myowndime.duoscoreboard.ui.common.SwapHorizIcon
import com.myowndime.duoscoreboard.ui.theme.DuoScoreboardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(
    state: ScoreboardUiState,
    playerOneName: String,
    playerTwoName: String,
    onIncrement: (Player) -> Unit,
    onDecrement: (Player) -> Unit,
    onResetConfirmed: () -> Unit,
    onSwapSides: () -> Unit,
    onSetScores: (playerOneScore: Int, playerTwoScore: Int) -> Unit,
    onFinishSetConfirmed: () -> Unit,
    onFinishConfirmed: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showSetScoreDialog by rememberSaveable { mutableStateOf(false) }
    var showFinishSetDialog by rememberSaveable { mutableStateOf(false) }
    var showFinishDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Once someone has won enough sets to take the match, surface the save prompt on
    // its own instead of waiting for the check button. Re-fires only on a fresh
    // null -> winner transition, so dismissing it once doesn't reopen it every tap.
    LaunchedEffect(state.matchWinner) {
        if (state.matchWinner != null) {
            showFinishDialog = true
        }
    }

    val shareCurrentScore = if (state.showSets) {
        stringResource(
            R.string.share_current_sets,
            playerOneName,
            state.playerOneSets,
            state.playerOneScore,
            playerTwoName,
            state.playerTwoSets,
            state.playerTwoScore,
        )
    } else {
        stringResource(
            R.string.share_current_score,
            playerOneName,
            state.playerOneScore,
            playerTwoName,
            state.playerTwoScore,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(
                        onClick = { showFinishSetDialog = true },
                        modifier = Modifier.testTag(ScoreboardTestTags.FINISH_SET_ACTION),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = stringResource(R.string.action_finish_set),
                        )
                    }
                    IconButton(
                        onClick = { showFinishDialog = true },
                        modifier = Modifier.testTag(ScoreboardTestTags.FINISH_ACTION),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(R.string.action_save_match),
                        )
                    }
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag(ScoreboardTestTags.HISTORY_ACTION),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.action_history),
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag(ScoreboardTestTags.OVERFLOW_ACTION),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.action_more),
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_share)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onShare(shareCurrentScore)
                                },
                                modifier = Modifier.testTag(ScoreboardTestTags.SHARE_ACTION),
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_settings)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onOpenSettings()
                                },
                                modifier = Modifier.testTag(ScoreboardTestTags.SETTINGS_ACTION),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // The app draws edge to edge, so the bar has to step around the
                    // gesture bar and any display cutout itself.
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                        ),
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    QuickAction(
                        icon = SwapHorizIcon,
                        label = stringResource(R.string.quick_swap_sides),
                        onClick = onSwapSides,
                        modifier = Modifier.testTag(ScoreboardTestTags.SWAP_ACTION),
                    )
                    QuickAction(
                        icon = Icons.Filled.Edit,
                        label = stringResource(R.string.quick_set_score),
                        onClick = { showSetScoreDialog = true },
                        modifier = Modifier.testTag(ScoreboardTestTags.SET_SCORE_ACTION),
                    )
                    QuickAction(
                        icon = Icons.Filled.Refresh,
                        label = stringResource(R.string.quick_reset),
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag(ScoreboardTestTags.RESET_ACTION),
                    )
                }
                Text(
                    text = stringResource(R.string.hint_gesture),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.showSets) {
                SetStatusBar(
                    playerOneName = playerOneName,
                    playerOneSets = state.playerOneSets,
                    playerOneColor = Color(state.settings.playerOne.colorArgb),
                    playerTwoName = playerTwoName,
                    playerTwoSets = state.playerTwoSets,
                    playerTwoColor = Color(state.settings.playerTwo.colorArgb),
                    matchFormatSets = state.settings.matchFormatSets,
                    swapped = state.sidesSwapped,
                )
            }
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                val panelSpacing = 12.dp
                val panelOne: @Composable (Modifier) -> Unit = { panelModifier ->
                    PlayerPanel(
                        player = Player.ONE,
                        name = playerOneName,
                        score = state.playerOneScore,
                        panelColor = Color(state.settings.playerOne.colorArgb),
                        onIncrement = { onIncrement(Player.ONE) },
                        onDecrement = { onDecrement(Player.ONE) },
                        modifier = panelModifier,
                        sets = state.playerOneSets,
                        showSets = state.showSets,
                        padScore = state.settings.padScoreDigits,
                    )
                }
                val panelTwo: @Composable (Modifier) -> Unit = { panelModifier ->
                    PlayerPanel(
                        player = Player.TWO,
                        name = playerTwoName,
                        score = state.playerTwoScore,
                        panelColor = Color(state.settings.playerTwo.colorArgb),
                        onIncrement = { onIncrement(Player.TWO) },
                        onDecrement = { onDecrement(Player.TWO) },
                        modifier = panelModifier,
                        sets = state.playerTwoSets,
                        showSets = state.showSets,
                        padScore = state.settings.padScoreDigits,
                    )
                }
                // Swapping only changes where each panel is drawn - scores, sets and
                // names stay attached to their player.
                val firstPanel = if (state.sidesSwapped) panelTwo else panelOne
                val secondPanel = if (state.sidesSwapped) panelOne else panelTwo

                if (maxWidth >= maxHeight) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(panelSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        firstPanel(Modifier.weight(1f).fillMaxSize())
                        secondPanel(Modifier.weight(1f).fillMaxSize())
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(panelSpacing),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        firstPanel(Modifier.weight(1f).fillMaxWidth())
                        secondPanel(Modifier.weight(1f).fillMaxWidth())
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_reset_title),
            message = stringResource(R.string.dialog_reset_message),
            onConfirm = {
                showResetDialog = false
                onResetConfirmed()
            },
            onDismiss = { showResetDialog = false },
        )
    }

    if (showFinishSetDialog) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_finish_set_title),
            message = stringResource(R.string.dialog_finish_set_message),
            onConfirm = {
                showFinishSetDialog = false
                onFinishSetConfirmed()
            },
            onDismiss = { showFinishSetDialog = false },
        )
    }

    if (showFinishDialog) {
        val winnerName = when (state.matchWinner) {
            Player.ONE -> playerOneName
            Player.TWO -> playerTwoName
            null -> null
        }
        ConfirmDialog(
            title = winnerName?.let { stringResource(R.string.dialog_match_won_title, it) }
                ?: stringResource(R.string.dialog_save_title),
            message = stringResource(R.string.dialog_save_message),
            onConfirm = {
                showFinishDialog = false
                onFinishConfirmed()
            },
            onDismiss = { showFinishDialog = false },
        )
    }

    if (showSetScoreDialog) {
        // The dialog's fields follow the screen order; map them back to players.
        val swapped = state.sidesSwapped
        SetScoreDialog(
            firstName = if (swapped) playerTwoName else playerOneName,
            firstScore = if (swapped) state.playerTwoScore else state.playerOneScore,
            secondName = if (swapped) playerOneName else playerTwoName,
            secondScore = if (swapped) state.playerOneScore else state.playerTwoScore,
            onConfirm = { first, second ->
                showSetScoreDialog = false
                if (swapped) onSetScores(second, first) else onSetScores(first, second)
            },
            onDismiss = { showSetScoreDialog = false },
        )
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, maxLines = 1)
    }
}

@Preview(name = "Scoreboard portrait", showBackground = true)
@Composable
private fun ScoreboardScreenPreview() {
    DuoScoreboardTheme {
        ScoreboardScreen(
            state = ScoreboardUiState(
                settings = AppSettings(setTargetScore = 21),
                playerOneScore = 7,
                playerTwoScore = 12,
                playerOneSets = 1,
                playerTwoSets = 0,
            ),
            playerOneName = "Player 1",
            playerTwoName = "Player 2",
            onIncrement = {},
            onDecrement = {},
            onResetConfirmed = {},
            onSwapSides = {},
            onSetScores = { _, _ -> },
            onFinishSetConfirmed = {},
            onFinishConfirmed = {},
            onOpenHistory = {},
            onOpenSettings = {},
            onShare = {},
        )
    }
}
