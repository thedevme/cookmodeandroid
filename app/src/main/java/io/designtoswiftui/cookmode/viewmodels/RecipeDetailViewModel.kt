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
import javax.inject.Inject

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<Step> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun loadRecipe(recipeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val recipeWithDetails = repository.getRecipeWithDetails(recipeId)
                if (recipeWithDetails != null) {
                    _uiState.update {
                        it.copy(
                            recipe = recipeWithDetails.recipe,
                            ingredients = recipeWithDetails.sortedIngredients,
                            steps = recipeWithDetails.sortedSteps,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Recipe not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load recipe"
                    )
                }
            }
        }
    }
}
