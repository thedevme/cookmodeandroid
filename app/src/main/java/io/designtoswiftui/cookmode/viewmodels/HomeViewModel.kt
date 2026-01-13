package io.designtoswiftui.cookmode.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.cookmode.data.PremiumManager
import io.designtoswiftui.cookmode.data.repository.RecipeRepository
import io.designtoswiftui.cookmode.models.Ingredient
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.models.Step
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recipes: List<Recipe> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val recipeCount: Int = 0
) {
    val isEmpty: Boolean
        get() = recipes.isEmpty() && searchQuery.isEmpty() && !isLoading

    val hasNoResults: Boolean
        get() = recipes.isEmpty() && searchQuery.isNotEmpty() && !isLoading
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    val isPremium: StateFlow<Boolean> = premiumManager.isPremium

    val recipes: StateFlow<List<Recipe>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllRecipes()
            } else {
                repository.searchRecipes(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recipeCount: StateFlow<Int> = repository.getRecipeCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun deleteRecipe(recipeId: Long) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
        }
    }

    fun canAddRecipe(): Boolean {
        return premiumManager.canAddRecipe(recipeCount.value)
    }

    fun getRemainingFreeRecipes(): Int {
        return premiumManager.getRemainingFreeRecipes(recipeCount.value)
    }

    // DEBUG: Add 3 test recipes for testing paywall
    fun debugAddTestRecipes() {
        viewModelScope.launch {
            // Test Recipe 1: Pasta
            repository.saveRecipe(
                recipe = Recipe(title = "Test Pasta", iconName = "pasta_bowl", prepTime = 30, servings = 4),
                ingredients = listOf(
                    Ingredient(recipeId = 0, amount = "1", unit = "lb", name = "Pasta"),
                    Ingredient(recipeId = 0, amount = "2", unit = "cups", name = "Tomato sauce"),
                    Ingredient(recipeId = 0, amount = "3", unit = "cloves", name = "Garlic")
                ),
                steps = listOf(
                    Step(recipeId = 0, instruction = "Boil pasta"),
                    Step(recipeId = 0, instruction = "Make sauce"),
                    Step(recipeId = 0, instruction = "Combine")
                )
            )

            // Test Recipe 2: Soup
            repository.saveRecipe(
                recipe = Recipe(title = "Test Soup", iconName = "hot_soup", prepTime = 45, servings = 6),
                ingredients = listOf(
                    Ingredient(recipeId = 0, amount = "4", unit = "cups", name = "Chicken broth"),
                    Ingredient(recipeId = 0, amount = "2", unit = "cups", name = "Vegetables"),
                    Ingredient(recipeId = 0, amount = "1", unit = "cup", name = "Noodles")
                ),
                steps = listOf(
                    Step(recipeId = 0, instruction = "Heat broth"),
                    Step(recipeId = 0, instruction = "Add veggies"),
                    Step(recipeId = 0, instruction = "Simmer", timerSeconds = 900)
                )
            )

            // Test Recipe 3: Salad
            repository.saveRecipe(
                recipe = Recipe(title = "Test Salad", iconName = "salad_bowl", prepTime = 10, servings = 2),
                ingredients = listOf(
                    Ingredient(recipeId = 0, amount = "1", unit = "head", name = "Lettuce"),
                    Ingredient(recipeId = 0, amount = "2", unit = "", name = "Tomatoes"),
                    Ingredient(recipeId = 0, amount = "2", unit = "tbsp", name = "Dressing")
                ),
                steps = listOf(
                    Step(recipeId = 0, instruction = "Chop veggies"),
                    Step(recipeId = 0, instruction = "Toss together"),
                    Step(recipeId = 0, instruction = "Add dressing")
                )
            )
        }
    }
}
