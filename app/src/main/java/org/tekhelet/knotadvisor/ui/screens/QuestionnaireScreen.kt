package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.Answer
import org.tekhelet.knotadvisor.model.Question
import org.tekhelet.knotadvisor.model.QuestionType
import org.tekhelet.knotadvisor.ui.AppViewModel

/**
 * כל שאלות השלב הראשי מוצגות כאן במסך אחד גלול, כדי שאפשר יהיה לראות את כל השאלון
 * בבת אחת ולחזור לתקן תשובה קודמת בלי לנווט קדימה-אחורה בין מסכים. שאלות סליידר
 * מקבלות ערך ניטרלי (5) כברירת מחדל כשהמסך נפתח, כך שכפתור הסיום תמיד זמין.
 */
@Composable
fun QuestionnaireScreen(
    viewModel: AppViewModel,
    onFinishedPrimaryQuestions: () -> Unit
) {
    val questions = viewModel.primaryQuestions

    LaunchedEffect(Unit) {
        questions
            .filter { it.type == QuestionType.SLIDER && viewModel.answerFor(it.id) == null }
            .forEach { viewModel.setAnswer(Answer(questionId = it.id, sliderValue = 5)) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("השאלון", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "כל השאלות במסך אחד - אפשר לגלול, לשנות תשובה בכל שלב, ולסיים מתי שנוח.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "בכל השאלות שיש בהן סרגל: 10 זה \"הכי חשוב לי\", 1 זה \"הכי פחות חשוב לי\".",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.height(24.dp))

            questions.forEachIndexed { index, question ->
                QuestionBlock(viewModel, question)
                if (index != questions.lastIndex) {
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(20.dp))
                }
            }
        }

        Surface(tonalElevation = 3.dp) {
            Button(
                onClick = onFinishedPrimaryQuestions,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("הצג תוצאות")
            }
        }
    }
}

@Composable
private fun QuestionBlock(viewModel: AppViewModel, question: Question) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(question.text, style = MaterialTheme.typography.titleMedium)
        question.helpText?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
        when (question.type) {
            QuestionType.SLIDER -> SliderQuestion(viewModel, question)
            QuestionType.SINGLE_CHOICE -> SingleChoiceQuestion(viewModel, question)
            QuestionType.MULTI_CHOICE -> MultiChoiceQuestion(viewModel, question)
            QuestionType.BOOLEAN -> BooleanQuestion(viewModel, question)
        }
    }
}

@Composable
private fun SliderQuestion(viewModel: AppViewModel, question: Question) {
    var value by remember(question.id) {
        mutableFloatStateOf((viewModel.answerFor(question.id)?.sliderValue ?: 5).toFloat())
    }
    Text("${value.toInt()}", style = MaterialTheme.typography.titleMedium)
    Slider(
        value = value,
        onValueChange = {
            value = it
            viewModel.setAnswer(Answer(questionId = question.id, sliderValue = it.toInt()))
        },
        valueRange = 1f..10f,
        steps = 8
    )
}

@Composable
private fun SingleChoiceQuestion(viewModel: AppViewModel, question: Question) {
    var selected by remember(question.id) {
        mutableStateOf(viewModel.answerFor(question.id)?.selectedOptionIds?.firstOrNull())
    }
    Column {
        question.options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = option.id == selected) {
                        selected = option.id
                        viewModel.setAnswer(Answer(questionId = question.id, selectedOptionIds = listOf(option.id)))
                    }
                    .padding(vertical = 6.dp)
            ) {
                RadioButton(selected = option.id == selected, onClick = {
                    selected = option.id
                    viewModel.setAnswer(Answer(questionId = question.id, selectedOptionIds = listOf(option.id)))
                })
                Spacer(Modifier.width(8.dp))
                Text(option.label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun MultiChoiceQuestion(viewModel: AppViewModel, question: Question) {
    var selected by remember(question.id) {
        mutableStateOf(viewModel.answerFor(question.id)?.selectedOptionIds?.toSet() ?: emptySet())
    }
    fun toggle(optionId: String) {
        selected = if (optionId in selected) selected - optionId else selected + optionId
        viewModel.setAnswer(Answer(questionId = question.id, selectedOptionIds = selected.toList()))
    }
    Column {
        question.options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = option.id in selected) { toggle(option.id) }
                    .padding(vertical = 6.dp)
            ) {
                Checkbox(checked = option.id in selected, onCheckedChange = { toggle(option.id) })
                Spacer(Modifier.width(8.dp))
                Text(option.label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun BooleanQuestion(viewModel: AppViewModel, question: Question) {
    var value by remember(question.id) { mutableStateOf(viewModel.answerFor(question.id)?.booleanValue) }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        FilterChip(
            selected = value == true,
            onClick = {
                value = true
                viewModel.setAnswer(Answer(questionId = question.id, booleanValue = true))
            },
            label = { Text("כן") }
        )
        FilterChip(
            selected = value == false,
            onClick = {
                value = false
                viewModel.setAnswer(Answer(questionId = question.id, booleanValue = false))
            },
            label = { Text("לא") }
        )
    }
}
