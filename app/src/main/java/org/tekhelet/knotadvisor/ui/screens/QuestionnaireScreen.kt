package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.Answer
import org.tekhelet.knotadvisor.model.Question
import org.tekhelet.knotadvisor.model.QuestionType
import org.tekhelet.knotadvisor.ui.AppViewModel

@Composable
fun QuestionnaireScreen(
    viewModel: AppViewModel,
    onFinishedPrimaryQuestions: () -> Unit
) {
    val questions = viewModel.primaryQuestions
    var index by rememberSaveable { mutableIntStateOf(0) }

    if (index >= questions.size) {
        LaunchedEffect(Unit) { onFinishedPrimaryQuestions() }
        return
    }

    val question = questions[index]
    val progress = (index + 1) / questions.size.toFloat()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Text("שאלה ${index + 1} מתוך ${questions.size}", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(24.dp))
        Text(question.text, style = MaterialTheme.typography.titleLarge)
        question.helpText?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(24.dp))

        var canProceed by remember(question.id) { mutableStateOf(hasExistingAnswer(viewModel, question)) }

        when (question.type) {
            QuestionType.SLIDER -> SliderQuestion(viewModel, question) { canProceed = true }
            QuestionType.SINGLE_CHOICE -> SingleChoiceQuestion(viewModel, question) { canProceed = true }
            QuestionType.BOOLEAN -> BooleanQuestion(viewModel, question) { canProceed = true }
            QuestionType.MULTI_CHOICE -> SingleChoiceQuestion(viewModel, question) { canProceed = true } // v0.1: מתנהג כבחירה יחידה
        }

        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { if (index > 0) index-- }, enabled = index > 0) {
                Text("הקודם")
            }
            Button(onClick = { index++ }, enabled = canProceed) {
                Text(if (index == questions.size - 1) "סיום" else "הבא")
            }
        }
    }
}

private fun hasExistingAnswer(viewModel: AppViewModel, question: Question): Boolean =
    viewModel.answerFor(question.id) != null

@Composable
private fun SliderQuestion(viewModel: AppViewModel, question: Question, onAnswered: () -> Unit) {
    var value by remember(question.id) {
        mutableFloatStateOf((viewModel.answerFor(question.id)?.sliderValue ?: 5).toFloat())
    }
    Text("1 = לא חשוב בכלל, 10 = חשוב מאוד. ערך נוכחי: ${value.toInt()}", style = MaterialTheme.typography.bodyMedium)
    Slider(
        value = value,
        onValueChange = {
            value = it
            viewModel.setAnswer(Answer(questionId = question.id, sliderValue = it.toInt()))
            onAnswered()
        },
        valueRange = 1f..10f,
        steps = 8
    )
}

@Composable
private fun SingleChoiceQuestion(viewModel: AppViewModel, question: Question, onAnswered: () -> Unit) {
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
                        onAnswered()
                    }
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(selected = option.id == selected, onClick = {
                    selected = option.id
                    viewModel.setAnswer(Answer(questionId = question.id, selectedOptionIds = listOf(option.id)))
                    onAnswered()
                })
                Spacer(Modifier.width(8.dp))
                Text(option.label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun BooleanQuestion(viewModel: AppViewModel, question: Question, onAnswered: () -> Unit) {
    var value by remember(question.id) { mutableStateOf(viewModel.answerFor(question.id)?.booleanValue) }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        FilterChip(
            selected = value == true,
            onClick = {
                value = true
                viewModel.setAnswer(Answer(questionId = question.id, booleanValue = true))
                onAnswered()
            },
            label = { Text("כן") }
        )
        FilterChip(
            selected = value == false,
            onClick = {
                value = false
                viewModel.setAnswer(Answer(questionId = question.id, booleanValue = false))
                onAnswered()
            },
            label = { Text("לא") }
        )
    }
}
