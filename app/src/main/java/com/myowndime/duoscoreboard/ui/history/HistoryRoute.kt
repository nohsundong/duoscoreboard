package com.myowndime.duoscoreboard.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myowndime.duoscoreboard.R
import com.myowndime.duoscoreboard.ui.AppViewModelProvider
import com.myowndime.duoscoreboard.ui.common.shareText

@Composable
fun HistoryRoute(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.share_chooser_title)

    HistoryScreen(
        matches = matches,
        onBack = onBack,
        onDelete = viewModel::delete,
        onDeleteAll = viewModel::clearAll,
        onShare = { text -> shareText(context, text, chooserTitle) },
    )
}
