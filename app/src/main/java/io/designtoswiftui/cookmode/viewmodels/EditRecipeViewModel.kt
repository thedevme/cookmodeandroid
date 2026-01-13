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

data class EditableIngredient(
    val id: String = UUID.randomUUID().toString(),
    val amount: String = "",
    val unit: String = "",
    val name: String = ""
) {
    val isValid: Boolean
        get() = name.isNotBlank()
}

data class EditableStep(
    val id: String = UUID.randomUUID().toString(),
    val instruction: String = "",
    val hasTimer: Boolean = false,
    val timerMinutes: Int = 0,
    val timerSeconds: Int = 0
) {
    val isValid: Boolean
        get() = instruction.isNotBlank()

    val timerTotalSeconds: Int?
        get() = if (hasTimer && (timerMinutes > 0 || timerSeconds > 0)) {
            timerMinutes * 60 + timerSeconds
        } else null
}

data class EditRecipeUiState(
    val recipeId: Long? = null,
    val title: String = "",
    val iconName: String = "hot_soup",
    val imageUri: String? = null,
    val prepTimeMinutes: Int = 0,
    val servings: Int = 1,
    val ingredients: List<EditableIngredient> = listOf(EditableIngredient()),
    val steps: List<EditableStep> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false
) {
    val isEditMode: Boolean
        get() = recipeId != null

    val validationErrors: List<String>
        get() = buildList {
            if (title.isBlank()) add("Title is required")
            if (steps.none { it.isValid }) add("At least one step is required")
        }

    val isValid: Boolean
        get() = validationErrors.isEmpty()

    val canSave: Boolean
        get() = isValid && !isSaving
}

@HiltViewModel
class EditRecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditRecipeUiState())
    val uiState: StateFlow<EditRecipeUiState> = _uiState.asStateFlow()

    fun loadRecipe(recipeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val recipeWithDetails = repository.getRecipeWithDetails(recipeId)
                if (recipeWithDetails != null) {
                    val editableIngredients = recipeWithDetails.sortedIngredients.map { ingredient ->
                        EditableIngredient(
                            id = ingredient.id.toString(),
                            amount = ingredient.amount,
                            unit = ingredient.unit,
                            name = ingredient.name
                        )
                    }.ifEmpty { listOf(EditableIngredient()) }

                    val editableSteps = recipeWithDetails.sortedSteps.map { step ->
                        val timerSeconds = step.timerSeconds ?: 0
                        EditableStep(
                            id = step.id.toString(),
                            instruction = step.instruction,
                            hasTimer = step.timerSeconds != null,
                            timerMinutes = timerSeconds / 60,
                            timerSeconds = timerSeconds % 60
                        )
                    }.ifEmpty { listOf(EditableStep()) }

                    _uiState.update {
                        it.copy(
                            recipeId = recipeId,
                            title = recipeWithDetails.recipe.title,
                            iconName = recipeWithDetails.recipe.iconName,
                            imageUri = recipeWithDetails.recipe.imageUri,
                            prepTimeMinutes = recipeWithDetails.recipe.prepTime,
                            servings = recipeWithDetails.recipe.servings,
                            ingredients = editableIngredients,
                            steps = editableSteps,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Recipe not found")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load recipe")
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateIconName(iconName: String) {
        _uiState.update { it.copy(iconName = iconName) }
    }

    fun updateImageUri(uri: String?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun updatePrepTime(minutes: Int) {
        _uiState.update { it.copy(prepTimeMinutes = minutes.coerceAtLeast(0)) }
    }

    fun updateServings(servings: Int) {
        _uiState.update { it.copy(servings = servings.coerceAtLeast(1)) }
    }

    // Ingredient operations
    fun addIngredient() {
        _uiState.update {
            it.copy(ingredients = it.ingredients + EditableIngredient())
        }
    }

    fun removeIngredient(id: String) {
        _uiState.update { state ->
            val newIngredients = state.ingredients.filter { it.id != id }
            state.copy(ingredients = newIngredients.ifEmpty { listOf(EditableIngredient()) })
        }
    }

    fun updateIngredient(id: String, amount: String? = null, unit: String? = null, name: String? = null) {
        _uiState.update { state ->
            state.copy(
                ingredients = state.ingredients.map { ingredient ->
                    if (ingredient.id == id) {
                        ingredient.copy(
                            amount = amount ?: ingredient.amount,
                            unit = unit ?: ingredient.unit,
                            name = name ?: ingredient.name
                        )
                    } else ingredient
                }
            )
        }
    }

    fun reorderIngredients(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val mutableList = state.ingredients.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            state.copy(ingredients = mutableList)
        }
    }

    // Step operations
    fun addStep() {
        _uiState.update {
            it.copy(steps = it.steps + EditableStep())
        }
    }

    fun removeStep(id: String) {
        _uiState.update { state ->
            val newSteps = state.steps.filter { it.id != id }
            state.copy(steps = newSteps)
        }
    }

    fun updateStep(
        id: String,
        instruction: String? = null,
        hasTimer: Boolean? = null,
        timerMinutes: Int? = null,
        timerSeconds: Int? = null
    ) {
        _uiState.update { state ->
            state.copy(
                steps = state.steps.map { step ->
                    if (step.id == id) {
                        step.copy(
                            instruction = instruction ?: step.instruction,
                            hasTimer = hasTimer ?: step.hasTimer,
                            timerMinutes = timerMinutes ?: step.timerMinutes,
                            timerSeconds = timerSeconds ?: step.timerSeconds
                        )
                    } else step
                }
            )
        }
    }

    fun reorderSteps(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val mutableList = state.steps.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            state.copy(steps = mutableList)
        }
    }

    fun saveRecipe() {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val recipe = Recipe(
                    id = state.recipeId ?: 0L,
                    title = state.title.trim(),
                    iconName = state.iconName,
                    imageUri = state.imageUri,
                    prepTime = state.prepTimeMinutes,
                    servings = state.servings
                )

                val ingredients = state.ingredients
                    .filter { it.isValid }
                    .mapIndexed { index, editable ->
                        Ingredient(
                            recipeId = 0L, // Will be set by repository
                            amount = editable.amount.trim(),
                            unit = editable.unit.trim(),
                            name = editable.name.trim(),
                            orderIndex = index
                        )
                    }

                val steps = state.steps
                    .filter { it.isValid }
                    .mapIndexed { index, editable ->
                        Step(
                            recipeId = 0L, // Will be set by repository
                            instruction = editable.instruction.trim(),
                            timerSeconds = editable.timerTotalSeconds,
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

    fun deleteRecipe() {
        val recipeId = _uiState.value.recipeId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }

            try {
                repository.deleteRecipe(recipeId)
                _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isDeleting = false, error = e.message ?: "Failed to delete recipe")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
