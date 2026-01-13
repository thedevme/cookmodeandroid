package io.designtoswiftui.cookmode.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.cookmode.data.repository.RecipeRepository
import io.designtoswiftui.cookmode.models.Ingredient
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.models.Step
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ParsedStep(
    val id: String = UUID.randomUUID().toString(),
    val instruction: String,
    val isEditing: Boolean = false
)

data class PasteRecipeUiState(
    val sourceText: String = "",
    val parsedSteps: List<ParsedStep> = emptyList(),
    val title: String = "",
    val isParsing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val hasParsed: Boolean = false
) {
    val canSave: Boolean
        get() = title.isNotBlank() && parsedSteps.isNotEmpty() && !isSaving

    val stepCount: Int
        get() = parsedSteps.size
}

@HiltViewModel
class PasteRecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasteRecipeUiState())
    val uiState: StateFlow<PasteRecipeUiState> = _uiState.asStateFlow()

    fun updateSourceText(text: String) {
        _uiState.update { it.copy(sourceText = text, hasParsed = false) }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun parseText() {
        val text = _uiState.value.sourceText.trim()
        if (text.isEmpty()) {
            _uiState.update { it.copy(parsedSteps = emptyList(), hasParsed = true) }
            return
        }

        _uiState.update { it.copy(isParsing = true) }

        val steps = smartParse(text)

        _uiState.update {
            it.copy(
                parsedSteps = steps.map { instruction -> ParsedStep(instruction = instruction) },
                isParsing = false,
                hasParsed = true
            )
        }
    }

    /**
     * Smart parsing algorithm that tries multiple patterns to split recipe text into steps.
     * Priority order:
     * 1. Numbered patterns: "1.", "2.", etc. or "1)", "2)", etc.
     * 2. "Step X:" or "Step X -" patterns
     * 3. Fallback: split by double newlines (paragraphs)
     */
    internal fun smartParse(text: String): List<String> {
        // Try numbered pattern first: "1.", "2.", "3." or "1)", "2)", "3)"
        val numberedDotPattern = Regex("""(?:^|\n)\s*(\d+)\.\s*""")
        val numberedParenPattern = Regex("""(?:^|\n)\s*(\d+)\)\s*""")

        // Try "Step X:" or "Step X -" pattern
        val stepColonPattern = Regex("""(?:^|\n)\s*[Ss]tep\s+\d+\s*[:：]\s*""")
        val stepDashPattern = Regex("""(?:^|\n)\s*[Ss]tep\s+\d+\s*[-–—]\s*""")

        // Check which pattern has the most matches
        val numberedDotMatches = numberedDotPattern.findAll(text).toList()
        val numberedParenMatches = numberedParenPattern.findAll(text).toList()
        val stepColonMatches = stepColonPattern.findAll(text).toList()
        val stepDashMatches = stepDashPattern.findAll(text).toList()

        return when {
            // Use numbered dot pattern if found multiple matches
            numberedDotMatches.size >= 2 -> {
                splitByPattern(text, numberedDotPattern)
            }
            // Use numbered paren pattern if found multiple matches
            numberedParenMatches.size >= 2 -> {
                splitByPattern(text, numberedParenPattern)
            }
            // Use step colon pattern if found multiple matches
            stepColonMatches.size >= 2 -> {
                splitByPattern(text, stepColonPattern)
            }
            // Use step dash pattern if found multiple matches
            stepDashMatches.size >= 2 -> {
                splitByPattern(text, stepDashPattern)
            }
            // At least one numbered dot pattern found
            numberedDotMatches.isNotEmpty() -> {
                splitByPattern(text, numberedDotPattern)
            }
            // At least one step colon pattern found
            stepColonMatches.isNotEmpty() -> {
                splitByPattern(text, stepColonPattern)
            }
            // Fallback: split by double newlines (paragraphs)
            else -> {
                splitByParagraphs(text)
            }
        }
    }

    private fun splitByPattern(text: String, pattern: Regex): List<String> {
        val matches = pattern.findAll(text).toList()
        if (matches.isEmpty()) return listOf(text.trim()).filter { it.isNotBlank() }

        val steps = mutableListOf<String>()

        // Check if there's text before the first match
        val firstMatchStart = matches.first().range.first
        if (firstMatchStart > 0) {
            val preamble = text.substring(0, firstMatchStart).trim()
            // Only add preamble if it's substantial (not just whitespace or a header)
            if (preamble.isNotBlank() && preamble.split(" ").size > 3) {
                steps.add(preamble)
            }
        }

        // Extract content between pattern matches
        for (i in matches.indices) {
            val matchEnd = matches[i].range.last + 1
            val nextStart = if (i + 1 < matches.size) {
                matches[i + 1].range.first
            } else {
                text.length
            }

            val content = text.substring(matchEnd, nextStart).trim()
            if (content.isNotBlank()) {
                steps.add(content)
            }
        }

        return steps
    }

    private fun splitByParagraphs(text: String): List<String> {
        // Split by double newlines, trimming each paragraph
        return text
            .split(Regex("""\n\s*\n"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { paragraph ->
                // Clean up single newlines within paragraph (make them spaces)
                paragraph.replace(Regex("""\s*\n\s*"""), " ").trim()
            }
    }

    fun editStep(stepId: String, newInstruction: String) {
        _uiState.update { state ->
            state.copy(
                parsedSteps = state.parsedSteps.map { step ->
                    if (step.id == stepId) {
                        step.copy(instruction = newInstruction.trim(), isEditing = false)
                    } else step
                }
            )
        }
    }

    fun setStepEditing(stepId: String, isEditing: Boolean) {
        _uiState.update { state ->
            state.copy(
                parsedSteps = state.parsedSteps.map { step ->
                    if (step.id == stepId) {
                        step.copy(isEditing = isEditing)
                    } else {
                        step.copy(isEditing = false)
                    }
                }
            )
        }
    }

    fun mergeSteps(stepId: String, withNextStep: Boolean = true) {
        _uiState.update { state ->
            val steps = state.parsedSteps.toMutableList()
            val index = steps.indexOfFirst { it.id == stepId }

            if (index == -1) return@update state

            val targetIndex = if (withNextStep) index + 1 else index - 1
            if (targetIndex < 0 || targetIndex >= steps.size) return@update state

            val currentStep = steps[index]
            val targetStep = steps[targetIndex]

            val mergedInstruction = if (withNextStep) {
                "${currentStep.instruction} ${targetStep.instruction}"
            } else {
                "${targetStep.instruction} ${currentStep.instruction}"
            }

            val mergedStep = currentStep.copy(instruction = mergedInstruction.trim())

            // Remove target step and update current
            steps.removeAt(targetIndex)
            val newIndex = if (withNextStep) index else index - 1
            steps[newIndex] = mergedStep

            state.copy(parsedSteps = steps)
        }
    }

    fun deleteStep(stepId: String) {
        _uiState.update { state ->
            state.copy(
                parsedSteps = state.parsedSteps.filter { it.id != stepId }
            )
        }
    }

    fun addManualStep() {
        _uiState.update { state ->
            state.copy(
                parsedSteps = state.parsedSteps + ParsedStep(
                    instruction = "",
                    isEditing = true
                )
            )
        }
    }

    fun reorderSteps(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val mutableList = state.parsedSteps.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            state.copy(parsedSteps = mutableList)
        }
    }

    fun saveRecipe() {
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val recipe = Recipe(
                    id = 0L,
                    title = state.title.trim(),
                    imageUri = null,
                    prepTime = 0,
                    servings = 1
                )

                val ingredients = emptyList<Ingredient>()

                val steps = state.parsedSteps
                    .filter { it.instruction.isNotBlank() }
                    .mapIndexed { index, parsed ->
                        Step(
                            recipeId = 0L,
                            instruction = parsed.instruction.trim(),
                            timerSeconds = null,
                            orderIndex = index
                        )
                    }

                repository.saveRecipe(recipe, ingredients, steps)

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = e.message ?: "Failed to save recipe")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
