package com.myowndime.duoscoreboard.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.myowndime.duoscoreboard.R
import com.myowndime.duoscoreboard.domain.model.MatchRecord
import com.myowndime.duoscoreboard.domain.model.MatchResult
import com.myowndime.duoscoreboard.ui.common.ConfirmDialog
import com.myowndime.duoscoreboard.ui.theme.DuoScoreboardTheme
import java.text.DateFormat
import java.util.Date
import java.util.Locale

object HistoryTestTags {
    const val LIST: String = "history_list"
    const val EMPTY: String = "history_empty"
    const val DELETE_ALL: String = "history_delete_all"
    const val SHARE: String = "history_share"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    matches: List<MatchRecord>,
    onBack: () -> Unit,
    onDelete: (Long) -> Unit,
    onDeleteAll: () -> Unit,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDeleteAllDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (matches.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteAllDialog = true },
                            modifier = Modifier.testTag(HistoryTestTags.DELETE_ALL),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(
                                    R.string.history_action_delete_all,
                                ),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (matches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.history_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag(HistoryTestTags.EMPTY),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag(HistoryTestTags.LIST),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = matches, key = { it.id }) { match ->
                    MatchRow(
                        match = match,
                        onDelete = { pendingDeleteId = match.id },
                        onShare = onShare,
                    )
                }
            }
        }
    }

    pendingDeleteId?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_one_title),
            message = stringResource(R.string.dialog_delete_one_message),
            onConfirm = {
                pendingDeleteId = null
                onDelete(id)
            },
            onDismiss = { pendingDeleteId = null },
        )
    }

    if (showDeleteAllDialog) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_all_title),
            message = stringResource(R.string.dialog_delete_all_message),
            onConfirm = {
                showDeleteAllDialog = false
                onDeleteAll()
            },
            onDismiss = { showDeleteAllDialog = false },
        )
    }
}

@Composable
private fun MatchRow(
    match: MatchRecord,
    onDelete: () -> Unit,
    onShare: (String) -> Unit,
) {
    val formatter = rememberDateTimeFormatter()
    val playedAt = formatter.format(Date(match.playedAtEpochMillis))
    val shareMessage = if (match.hasSets) {
        stringResource(
            R.string.share_match_result_sets,
            match.playerOneName,
            match.playerOneSets,
            match.playerTwoName,
            match.playerTwoSets,
            playedAt,
        )
    } else {
        stringResource(
            R.string.share_match_result,
            match.playerOneName,
            match.playerOneScore,
            match.playerTwoName,
            match.playerTwoScore,
            playedAt,
        )
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (match.result) {
                        MatchResult.DRAW -> stringResource(R.string.history_draw)
                        else -> stringResource(R.string.history_winner, match.winnerName.orEmpty())
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                if (match.hasSets) {
                    Text(
                        text = stringResource(
                            R.string.history_sets,
                            match.playerOneSets,
                            match.playerTwoSets,
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                if (!match.hasSets || match.playerOneScore > 0 || match.playerTwoScore > 0) {
                    Text(
                        text = stringResource(
                            R.string.history_score,
                            match.playerOneName,
                            match.playerOneScore,
                            match.playerTwoScore,
                            match.playerTwoName,
                        ),
                        style = if (match.hasSets) {
                            MaterialTheme.typography.bodyMedium
                        } else {
                            MaterialTheme.typography.headlineSmall
                        },
                    )
                }
                Text(
                    text = playedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = { onShare(shareMessage) },
                modifier = Modifier.testTag(HistoryTestTags.SHARE),
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.action_share),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.history_action_delete),
                )
            }
        }
    }
}

@Composable
private fun rememberDateTimeFormatter(): DateFormat {
    val configuration = LocalConfiguration.current
    val locale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    return remember(locale) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale)
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    DuoScoreboardTheme {
        HistoryScreen(
            matches = listOf(
                MatchRecord(
                    id = 1,
                    playerOneName = "Player 1",
                    playerTwoName = "Player 2",
                    playerOneScore = 21,
                    playerTwoScore = 18,
                    playedAtEpochMillis = 1_700_000_000_000,
                ),
            ),
            onBack = {},
            onDelete = {},
            onDeleteAll = {},
            onShare = {},
        )
    }
}
