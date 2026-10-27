package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.myowndime.duoscoreboard.R

object SetScoreTestTags {
    const val FIRST_FIELD: String = "set_score_first"
    const val SECOND_FIELD: String = "set_score_second"
    const val CONFIRM: String = "set_score_confirm"
}

/**
 * Lets the user type both running scores directly (0..[ScoreboardViewModel.MAX_SCORE]),
 * for resuming a game halfway through or correcting a long run of missed taps.
 *
 * Values are in the order the panels are drawn, so the fields line up with the screen
 * even after the sides have been swapped.
 */
@Composable
fun SetScoreDialog(
    firstName: String,
    firstScore: Int,
    secondName: String,
    secondScore: Int,
    onConfirm: (firstScore: Int, secondScore: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var firstText by rememberSaveable { mutableStateOf(firstScore.toString()) }
    var secondText by rememberSaveable { mutableStateOf(secondScore.toString()) }

    val first = firstText.toIntOrNull()
    val second = secondText.toIntOrNull()
    val valid = first != null && second != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_set_score_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_set_score_message),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                ScoreField(
                    label = firstName,
                    value = firstText,
                    onValueChange = { firstText = it.sanitizedScore() },
                    imeAction = ImeAction.Next,
                    modifier = Modifier.testTag(SetScoreTestTags.FIRST_FIELD),
                )
                ScoreField(
                    label = secondName,
                    value = secondText,
                    onValueChange = { secondText = it.sanitizedScore() },
                    imeAction = ImeAction.Done,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .testTag(SetScoreTestTags.SECOND_FIELD),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (first != null && second != null) onConfirm(first, second) },
                enabled = valid,
                modifier = Modifier.testTag(SetScoreTestTags.CONFIRM),
            ) {
                Text(stringResource(R.string.dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

@Composable
private fun ScoreField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1) },
        supportingText = { Text(stringResource(R.string.dialog_set_score_range)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Keeps digits only and caps the length so a value can never exceed the maximum score. */
private fun String.sanitizedScore(): String =
    filter { it.isDigit() }.take(MAX_SCORE_DIGITS)

private val MAX_SCORE_DIGITS: Int = ScoreboardViewModel.MAX_SCORE.toString().length
